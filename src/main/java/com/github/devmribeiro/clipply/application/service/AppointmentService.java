package com.github.devmribeiro.clipply.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.request.AppointmentRequest;
import com.github.devmribeiro.clipply.application.dto.response.AppointmentResponse;
import com.github.devmribeiro.clipply.application.dto.response.AvailableSlotsResponse;
import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.Appointment;
import com.github.devmribeiro.clipply.application.model.Company;
import com.github.devmribeiro.clipply.application.model.Customer;
import com.github.devmribeiro.clipply.application.model.Product;
import com.github.devmribeiro.clipply.application.model.Schedule;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.repository.AppointmentRepository;
import com.github.devmribeiro.clipply.application.repository.CompanyRepository;
import com.github.devmribeiro.clipply.application.repository.ProductRepository;
import com.github.devmribeiro.clipply.application.repository.ScheduleRepository;
import com.github.devmribeiro.clipply.application.repository.UserRepository;
import com.github.devmribeiro.clipply.application.type.AppointmentStatus;
import com.github.devmribeiro.clipply.application.type.DayOfWeek;
import com.github.devmribeiro.clipply.security.model.UserDetailsImpl;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

import jakarta.transaction.Transactional;

@Service
public class AppointmentService {

    @Value("${clipply.base-url}")
    private String baseUrl;

    private final AppointmentRepository appointmentRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final CustomerService customerService;
    private final EmailService emailService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            ScheduleRepository scheduleRepository,
            UserRepository userRepository,
            CustomerService customerService,
            EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.customerService = customerService;
        this.emailService = emailService;
    }

    public AvailableSlotsResponse getAvailableSlots(String slug, UUID professionalId, UUID productId, LocalDate date) {

        Company company = companyRepository.findBySlug(slug);

        if (company == null)
            throw new IllegalArgumentException("Company not found");

        if (!company.getActive())
            throw new IllegalArgumentException("Company is not active");

        Product product = productRepository.findById(productId).orElse(null);

        if (product == null || !product.getCompany().equals(company.getId()))
            throw new IllegalArgumentException("Product not found");

        if (!product.getActive())
            throw new IllegalArgumentException("Product is not active");

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(date.getDayOfWeek().name());
        Schedule schedule = scheduleRepository.findByCompanyIdAndDayOfWeek(company.getId(), dayOfWeek);

        if (schedule == null)
            throw new IllegalArgumentException("Company does not work on this day");

        List<LocalTime> slots = new ArrayList<LocalTime>();
        LocalTime current = schedule.getStartTime();
        LocalTime end = schedule.getEndTime();
        int duration = product.getDurationMinutes();

        while (!current.plusMinutes(duration).isAfter(end)) {
            LocalDateTime slotStart = LocalDateTime.of(date, current);
            LocalDateTime slotEnd = slotStart.plusMinutes(duration);

            List<Appointment> conflicts = appointmentRepository.findConflicts(professionalId, slotStart, slotEnd);

            if (conflicts.isEmpty())
                slots.add(current);

            current = current.plusMinutes(duration);
        }

        return new AvailableSlotsResponse(slots);
    }

    @Transactional
    public void create(String slug, AppointmentRequest request) {

        Company company = companyRepository.findBySlug(slug);

        if (company == null)
            throw new IllegalArgumentException("Company not found");

        if (!company.getActive())
            throw new IllegalArgumentException("Company is not active");

        Product product = productRepository.findById(request.productId()).orElse(null);

        if (product == null || !product.getCompany().equals(company.getId()))
            throw new IllegalArgumentException("Product not found");

        if (!product.getActive())
            throw new IllegalArgumentException("Product is not active");

        User professional = userRepository.findById(request.professionalId()).orElse(null);

        if (professional == null || !professional.getCompanyId().equals(company.getId()))
            throw new IllegalArgumentException("Professional not found");

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.date().getDayOfWeek().name());
        Schedule schedule = scheduleRepository.findByCompanyIdAndDayOfWeek(company.getId(), dayOfWeek);

        if (schedule == null)
            throw new IllegalArgumentException("Company does not work on this day");

        LocalDateTime startTime = LocalDateTime.of(request.date(), request.startTime());
        LocalDateTime endTime = startTime.plusMinutes(product.getDurationMinutes());

        if (request.startTime().isBefore(schedule.getStartTime()) ||
                endTime.toLocalTime().isAfter(schedule.getEndTime()))
            throw new IllegalArgumentException("Time slot is outside company working hours");

        List<Appointment> conflicts = appointmentRepository.findConflicts(
                request.professionalId(), startTime, endTime);

        if (!conflicts.isEmpty())
            throw new IllegalArgumentException("This time slot is already taken");

        Customer customer = customerService.findOrCreate(request.customerName(), request.customerPhone());

        String token = UUID.randomUUID().toString();

        Appointment appointment = new Appointment();
        appointment.setCompanyId(company.getId());
        appointment.setCustomerId(customer.getId());
        appointment.setProductId(product.getId());
        appointment.setUserId(request.professionalId());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setToken(token);
        appointmentRepository.save(appointment);

        if (customer.getEmail() != null) {
            String cancelUrl = baseUrl + "/api/public/appointment/cancel/" + token;
            String formattedTime = startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            emailService.sendAppointmentConfirmation(
                customer.getEmail(),
                customer.getName(),
                company.getName(),
                product.getName(),
                professional.getName(),
                formattedTime,
                cancelUrl
            );
        }
    }

    public void cancel(String token) {
        Appointment appointment = appointmentRepository.findByToken(token);

        if (appointment == null)
            throw new IllegalArgumentException("Appointment not found");

        if (appointment.getStatus() == AppointmentStatus.CANCELLED)
            throw new IllegalArgumentException("Appointment is already cancelled");

        if (appointment.getStatus() == AppointmentStatus.COMPLETED)
            throw new IllegalArgumentException("Completed appointments cannot be cancelled");

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    public void confirm(UUID appointmentId) {
        UUID companyId = SecurityUtils.getCompanyId();
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

        if (appointment == null || !appointment.getCompanyId().equals(companyId))
            throw new IllegalArgumentException("Appointment not found");

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED)
            throw new IllegalArgumentException("Only SCHEDULED appointments can be confirmed");

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    public void complete(UUID appointmentId) {
        UUID companyId = SecurityUtils.getCompanyId();
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

        if (appointment == null || !appointment.getCompanyId().equals(companyId))
            throw new IllegalArgumentException("Appointment not found");

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED)
            throw new IllegalArgumentException("Only CONFIRMED appointments can be completed");

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    public void noShow(UUID appointmentId) {
        UUID companyId = SecurityUtils.getCompanyId();
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

        if (appointment == null || !appointment.getCompanyId().equals(companyId))
            throw new IllegalArgumentException("Appointment not found");

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED)
            throw new IllegalArgumentException("Only CONFIRMED appointments can be marked as no-show");

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.save(appointment);
    }

    public List<AppointmentResponse> listByCompany() {
        UUID companyId = SecurityUtils.getCompanyId();
        List<Appointment> appointments = appointmentRepository.findActiveByCompanyId(companyId);
        return toResponseList(appointments);
    }

    public List<AppointmentResponse> listTodayByCompany() {
        UUID companyId = SecurityUtils.getCompanyId();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        List<Appointment> appointments = appointmentRepository.findActiveByCompanyIdAndDate(
                companyId, startOfDay, endOfDay);
        return toResponseList(appointments);
    }

    public List<AppointmentResponse> listTodayByProfessional() {
        UserDetailsImpl user = SecurityUtils.getAuthenticatedUser();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        List<Appointment> appointments = appointmentRepository.findActiveByUserIdAndDate(user.getId(), startOfDay, endOfDay);
        return toResponseList(appointments);
    }

    private List<AppointmentResponse> toResponseList(List<Appointment> appointments) {
        List<AppointmentResponse> result = new ArrayList<AppointmentResponse>(appointments.size());

        for (Appointment appointment : appointments) {
            User professional = userRepository.findById(appointment.getUserId()).orElse(null);
            Customer customer = customerService.findById(appointment.getCustomerId());
            Product product = productRepository.findById(appointment.getProductId()).orElse(null);

            result.add(new AppointmentResponse(
                appointment.getId(),
                customer != null ? customer.getName() : "",
                customer != null ? customer.getPhone() : "",
                product != null ? product.getName() : "",
                professional != null ? professional.getName() : "",
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus()
            ));
        }

        return result;
    }
}