package com.github.devmribeiro.clipply.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.github.devmribeiro.clipply.messaging.service.EmailService;
import com.github.devmribeiro.clipply.security.model.UserDetailsImpl;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

import jakarta.transaction.Transactional;

@Service
public class AppointmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentService.class);
	
    @Value("${clipply.base-url}")
    private String baseUrl;

    private final AppointmentRepository appointmentRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final CustomerService customerService;
    private final EmailService emailService;
    private final CompanySettingsService companySettingsService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            ScheduleRepository scheduleRepository,
            UserRepository userRepository,
            CustomerService customerService,
            EmailService emailService,
            CompanySettingsService companySettingsService) {
        this.appointmentRepository = appointmentRepository;
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.customerService = customerService;
        this.emailService = emailService;
		this.companySettingsService = companySettingsService;
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
        List<Schedule> schedules = scheduleRepository.findByCompanyIdAndDayOfWeek(company.getId(), dayOfWeek);

        if (schedules.isEmpty())
            throw new IllegalArgumentException("Company does not work on this day");

        int duration = product.getDurationMinutes();
        List<LocalTime> slots = new ArrayList<LocalTime>();

        LocalDateTime now = LocalDateTime.now();
        
        int i = 0;
        while (i < schedules.size()) {
            Schedule schedule = schedules.get(i);
            LocalTime current = schedule.getStartTime();
            LocalTime end = schedule.getEndTime();

            while (!current.plusMinutes(duration).isAfter(end)) {
                LocalDateTime slotStart = LocalDateTime.of(date, current);
                LocalDateTime slotEnd = slotStart.plusMinutes(duration);

                if (date.equals(now.toLocalDate()) && slotStart.isBefore(now)) {
                    current = current.plusMinutes(duration);
                    continue;
                }
                
                List<Appointment> conflicts = appointmentRepository.findConflicts(professionalId, slotStart, slotEnd);

                if (conflicts.isEmpty())
                    slots.add(current);

                current = current.plusMinutes(duration);
            }

            i++;
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

        LocalDateTime requestDateTime = LocalDateTime.of(request.date(), request.startTime());

    	if (requestDateTime.isBefore(LocalDateTime.now()))
    		throw new IllegalArgumentException("The time must be in the future");
    	
    	if (request.date().isAfter(LocalDateTime.now().plusDays(companySettingsService.getShcedulingHorizon(company.getId())).toLocalDate()))
    	    throw new IllegalArgumentException("Date exceeds scheduling horizon");
        
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.date().getDayOfWeek().name());
        List<Schedule> schedules = scheduleRepository.findByCompanyIdAndDayOfWeek(company.getId(), dayOfWeek);

        if (schedules.isEmpty())
            throw new IllegalArgumentException("Company does not work on this day");

        LocalDateTime startTime = LocalDateTime.of(request.date(), request.startTime());
        LocalDateTime endTime = startTime.plusMinutes(product.getDurationMinutes());

        boolean withinSchedule = false;
        int i = 0;
        while (i < schedules.size()) {
            Schedule schedule = schedules.get(i);
            if (!request.startTime().isBefore(schedule.getStartTime()) &&
                    !endTime.toLocalTime().isAfter(schedule.getEndTime())) {
                withinSchedule = true;
                break;
            }
            i++;
        }

        if (!withinSchedule)
            throw new IllegalArgumentException("Time slot is outside company working hours");

        List<Appointment> conflicts = appointmentRepository.findConflicts(request.professionalId(), startTime, endTime);

        if (!conflicts.isEmpty())
            throw new IllegalArgumentException("This time slot is already taken");

        Customer customer = customerService.findOrCreate(request.customerName(), request.customerPhone(), request.customerEmail());

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

        sendEmailConfirmation(customer, company, product, professional, startTime, token);
    }

    private void sendEmailConfirmation(Customer customer, Company company, Product product, User professional, LocalDateTime startTime, String token) {
        String cancelUrl = baseUrl + "/api/public/appointment/cancel/" + token;
        String formattedTime = startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        Map<String, String> vars = Map.of(
                "COMPANY_NAME", company.getName(),
                "CLIENT_NAME", customer.getName(),
                "SERVICE_NAME", product.getName(),
                "PROFESSIONAL_NAME", professional.getName(),
                "APPOINTMENT_DATE", formattedTime,
                "CANCEL_LINK", cancelUrl,
                "YEAR", String.valueOf(LocalDateTime.now().getYear())
        );

        emailService.sendAppointmentConfirmedEmail(customer.getEmail(), vars);
    }

    // Cancelamento via token público (pelo cliente através do link no e-mail)
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

    // Cancelamento autenticado (pelo admin ou profissional no painel)
    public void cancelByAdmin(UUID appointmentId) {
        UUID companyId = SecurityUtils.getCompanyId();

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

        if (appointment == null || !appointment.getCompanyId().equals(companyId))
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
        List<Appointment> appointments = appointmentRepository.findActiveByCompanyIdAndDate(companyId, startOfDay, endOfDay);
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

        int i = 0;
        while (i < appointments.size()) {
            Appointment appointment = appointments.get(i);
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

            i++;
        }

        return result;
    }
    
    @Transactional
    public void processDueAppointments() {
    	LOGGER.info("[Update Status] - Rows Affected -> {}", appointmentRepository.updateStatus(LocalDateTime.now(), AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED));
    }
}