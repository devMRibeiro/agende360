package br.com.corestacks.agende360.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;

import br.com.corestacks.agende360.application.dto.request.AppointmentRequest;
import br.com.corestacks.agende360.application.dto.response.AppointmentResponse;
import br.com.corestacks.agende360.application.dto.response.AvailableSlotsResponse;
import br.com.corestacks.agende360.application.exception.IllegalArgumentException;
import br.com.corestacks.agende360.application.model.Appointment;
import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.model.Customer;
import br.com.corestacks.agende360.application.model.Product;
import br.com.corestacks.agende360.application.model.Schedule;
import br.com.corestacks.agende360.application.model.User;
import br.com.corestacks.agende360.application.repository.AppointmentRepository;
import br.com.corestacks.agende360.application.repository.CompanyRepository;
import br.com.corestacks.agende360.application.repository.ProductRepository;
import br.com.corestacks.agende360.application.repository.ScheduleRepository;
import br.com.corestacks.agende360.application.repository.UserRepository;
import br.com.corestacks.agende360.application.type.AppointmentStatus;
import br.com.corestacks.agende360.application.type.DayOfWeek;
import br.com.corestacks.agende360.application.type.SchedulingHorizon;
import br.com.corestacks.agende360.application.util.BaseUrlUtils;
import br.com.corestacks.agende360.messaging.email.dto.AppointmentReminderEmail;
import br.com.corestacks.agende360.messaging.whatsapp.dto.WhatsAppAppointmentReminder;
import br.com.corestacks.agende360.outbox.enums.AggregateType;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.factory.OutboxEventFactory;
import br.com.corestacks.agende360.outbox.service.OutboxEventService;
import br.com.corestacks.agende360.security.model.UserDetailsImpl;
import br.com.corestacks.agende360.security.util.SecurityUtils;
import jakarta.transaction.Transactional;

@Service
public class AppointmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentService.class);
	
    private final AppointmentRepository appointmentRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final CustomerService customerService;
    private final CompanySettingsService companySettingsService;
    private final OutboxEventService outboxEventService;
    private final OutboxEventFactory outboxEventFactory;
    private final CompanyService companyService;

    private final Cache<UUID, Map<UUID, Product>> productsCache;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            ScheduleRepository scheduleRepository,
            UserRepository userRepository,
            CustomerService customerService,
            CompanySettingsService companySettingsService,
            Cache<UUID, Map<UUID, Product>> productsCache,
            OutboxEventService outboxEventService,
            OutboxEventFactory outboxEventFactory,
            CompanyService companyService) {
        this.appointmentRepository = appointmentRepository;
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.customerService = customerService;
		this.companySettingsService = companySettingsService;
		this.outboxEventService = outboxEventService;
		this.outboxEventFactory = outboxEventFactory;
		this.companyService = companyService;
		this.productsCache = productsCache;
    }

    public AvailableSlotsResponse getAvailableSlots(String slug, UUID professionalId, UUID productId, LocalDate date) {

    	Company company = companyService.findByCompanySlug(slug);
    	
        if (company == null || !company.getActive())
    		throw new IllegalArgumentException("Company not found or Company is not active");

        Product product = productsCache.getIfPresent(company.getId()).get(productId);
        
        if (product == null) {
        	product = productRepository.findById(productId).orElse(null);
        	
        	if (product == null || !product.getCompanyId().equals(company.getId()) || !product.getActive())
				throw new IllegalArgumentException("Product not found");
        }

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(date.getDayOfWeek().name());
        List<Schedule> schedules = scheduleRepository.findByCompanyIdAndDayOfWeek(company.getId(), dayOfWeek);

        if (schedules.isEmpty())
            throw new IllegalArgumentException("Company does not work on this day");

        int duration = product.getDurationMinutes();
        List<LocalTime> slots = new ArrayList<LocalTime>();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Appointment> appointments = appointmentRepository.findAppointmentsInPeriod(professionalId, startOfDay, endOfDay);
        
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

                boolean hasConflict = false;

                for (Appointment appointment : appointments) {
                    if (slotStart.isBefore(appointment.getEndTime()) && slotEnd.isAfter(appointment.getStartTime())) {
                        hasConflict = true;
                        break;
                    }
                }

                if (!hasConflict)
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

        if (product == null || !product.getCompanyId().equals(company.getId()))
            throw new IllegalArgumentException("Product not found");

        if (!product.getActive())
            throw new IllegalArgumentException("Product is not active");

        User professional = userRepository.findProfessionalById(request.professionalId(), company.getId());

        if (professional == null || !professional.getCompanyId().equals(company.getId()))
            throw new IllegalArgumentException("Professional not found");

        LocalDateTime requestDateTime = LocalDateTime.of(request.date(), request.startTime());

    	if (requestDateTime.isBefore(LocalDateTime.now()))
    		throw new IllegalArgumentException("The time must be in the future");
    	
    	int schedulingHorizon = companySettingsService.getShcedulingHorizon(company.getId());
    	
    	if (schedulingHorizon != SchedulingHorizon.SEM_LIMITE.getValue()) {
    		if (request.date().isAfter(LocalDateTime.now().plusDays(schedulingHorizon).toLocalDate()))
    			throw new IllegalArgumentException("Date exceeds scheduling horizon");
    	}
        
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
        customer.setName(request.customerName());

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
        
        criaLembretes(appointment, customer, company, product, professional);
    }
    
    private AppointmentReminderEmail buildEmailPayload(Company company, Customer customer, Product product, User professional, Appointment appointment) {
    	return new AppointmentReminderEmail(
    			company.getName(),
				company.getSlug(),
				customer.getName(),
				customer.getEmail(),
				product.getName(),
				professional.getName(),
				BaseUrlUtils.HOST + "/appointment/" + company.getSlug() + "/cancel/" + appointment.getToken(),
				appointment.getStartTime(),
				company.getEndereco()
		);
    }
    
    private WhatsAppAppointmentReminder buildWhatsAppPayload(Customer customer, Appointment appointment, Company company, Product product, User professional) {
    	return new WhatsAppAppointmentReminder(
				customer.getPhone(),
				customer.getName(),
				appointment.getStartTime(),
				company.getEndereco().toString(),
				product.getName(),
				product.getDescription(),
				professional.getName(),
				company.getName(),
				company.getSlug(),
				appointment.getToken()
		);
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
            		professional.getId(),
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
    
    private void criaLembretes(Appointment appointment, Customer customer, Company company, Product product, User professional) {
    	
    	LocalDateTime apptDate = appointment.getStartTime();
        LocalDateTime lembrete24h = apptDate.minusHours(24);

        // Se a data do agendamento for menor do que 24 horas de NOW, define nextAttemptAt para NOW.
        LocalDateTime nextAttemptAt = lembrete24h.isBefore(LocalDateTime.now()) ? nextAttemptAt = LocalDateTime.now()
        																  		: lembrete24h;
    	
		outboxEventService.saveAll(List.of(
				outboxEventFactory.create(AggregateType.APPOINTMENT, appointment.getId(), OutboxEventType.WHATSAPP_APPOINTMENT_REMINDER, buildWhatsAppPayload(customer, appointment, company, product, professional), nextAttemptAt),
				outboxEventFactory.create(AggregateType.APPOINTMENT, appointment.getId(), OutboxEventType.EMAIL_APPOINTMENT_REMINDER, 	 buildEmailPayload(company, customer, product, professional, appointment), nextAttemptAt)
		));
    }
}