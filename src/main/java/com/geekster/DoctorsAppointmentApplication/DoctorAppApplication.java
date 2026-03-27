package com.geekster.DoctorsAppointmentApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.geekster.DoctorsAppointmentApplication.model.Admin;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import com.geekster.DoctorsAppointmentApplication.repository.IAdminRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;

@SpringBootApplication
public class DoctorAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoctorAppApplication.class, args);
	}

	@Bean
	CommandLineRunner initDemoData(IDoctorRepo doctorRepo, IAdminRepo adminRepo) {
		return args -> {
			// Check if demo doctor exists
			Doctor existing = doctorRepo.findFirstByDoctorEmail("doctor@test.com");
			if (existing == null) {
				// Add demo doctor
				Doctor demoDoctor = new Doctor("Dr. Smith", "doctor@test.com", "482C811DA5D5B4BC6D497FFA98491E38", Specialization.Internal_Medicine);
				doctorRepo.save(demoDoctor);
				System.out.println("Demo doctor added: doctor@test.com / doctor123");
			} else {
				System.out.println("Demo doctor already exists: " + existing.getDoctorEmail());
			}

			// Check if admin exists
			Admin existingAdmin = adminRepo.findByAdminEmail("admin@hospital.com");
			if (existingAdmin == null) {
				// Add default admin
				Admin defaultAdmin = new Admin("admin@hospital.com", "admin123", "System Administrator");
				adminRepo.save(defaultAdmin);
				System.out.println("Default admin added: admin@hospital.com / admin123");
			} else {
				System.out.println("Admin already exists: " + existingAdmin.getAdminEmail());
			}
		};
	}

}
