package main.java.interview.treatment.plan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;

import test.java.interview.treatment.plan.TestHelper;

/**
 * Your task is to implement the below service to solve the following problem:
 * given a Patient, what is the appropriate TreatmentPlan?
 *
 * A Patient has a name, date of birth, weight, list of symptoms, list of
 * medication allergies, and MRN (Medical Record Number). We have also provided
 * a list of Diseases, Medications, and Clinics for use in this problem in our
 * test suite.
 *
 * A Disease has a name, list of symptoms (which suggest a patient has the
 * disease if a patient has the symptoms in the list), and a list of possible
 * treatments for the disease. Each possible treatment for a disease is a
 * combination of medications with dosage amounts given in mg/kg.
 *
 * A Medication has a name and a cost per mg.
 *
 * A Clinic has a name, a range of ages (in months) that the clinic is open to,
 * and a list of diseases the clinic specializes in treating.
 *
 * Using this information and the provided classes and interface, implement the
 * TreatmentPlanServiceImpl class. Each method in the interface includes exact
 * specifications for what it should return. You can validate that you are
 * returning the correct information using the provided JUnit Test Suite. We
 * will test your answers against additional tests upon your submission of your
 * code.
 *
 * The "Init" method will be called before each test to set up the lists of
 * Disease, Medications, and Clinics. We may test your solution against
 * different lists of Diseases, Medications, and Clinics.
 */
public class TreatmentPlanServiceImpl implements TreatmentPlanService {

	// Do not modify the lists below.
	private List<Disease> diseases = new ArrayList<>();
	private List<Medication> medications = new ArrayList<>();
	private List<Clinic> clinics = new ArrayList<>();
	private LocalDate localDate = LocalDate.of(2016, 9, 1);
	private BigDecimal diseasePossibility = new BigDecimal("0.70");

	// TODO Optionally Implement any additional data structures here....

	// TODO .... to here.

	@Override
	public void init(List<Disease> diseases, List<Clinic> clinics,
			List<Medication> medications) {

		this.diseases = diseases;
		this.clinics = clinics;
		this.medications = medications;

		// TODO Optionally implement any additional init items below here ....

		// TODO ... to here.
	}

	@Override
	public Integer ageInYears(Patient patient) {
		if (patient != null && patient.getDateOfBirth() != null
				&& patient.getDateOfBirth().compareTo(localDate) <= 0) {
			return (int) ChronoUnit.YEARS.between(patient.getDateOfBirth(),
					localDate);
		} else {
			throw new UnsupportedOperationException("Not Valid Date of Birth");
		}

	}

	@Override
	public Integer ageInMonths(Patient patient) {
		if (patient != null && patient.getDateOfBirth() != null
				&& patient.getDateOfBirth().compareTo(localDate) <= 0) {
			return (int) ChronoUnit.MONTHS.between(patient.getDateOfBirth(),
					localDate);
		} else {
			throw new UnsupportedOperationException("Not Valid Date of Birth");
		}
	}

	@Override
	public List<Clinic> clinicsBasedOnAgeAndDiseases(Patient patient) {

		List<Clinic> resultClinic = new ArrayList<Clinic>();
		try {
			if (patient != null) {
				Map<Disease, BigDecimal> possibleDisease = diseaseLikelihoods(patient);
				for (Disease disease : diseases) {

					if (possibleDisease.get(disease) != null
							&& possibleDisease.get(disease).compareTo(
									diseasePossibility) >= 0) {

						for (Clinic c : clinics) {
							if (!c.getDiseases().isEmpty()
									&& c.getDiseases().contains(
											disease.getName())
									&& c.getMinAgeInMonths() <= ageInMonths(patient)
									&& (c.getMaxAgeInMonths() == null || c
											.getMaxAgeInMonths() > ageInMonths(patient))) {
								if (resultClinic.isEmpty()) {
									resultClinic.add(c);
								} else if (!resultClinic.contains(c)) {
									resultClinic.add(c);
								}

							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new UnsupportedOperationException(e.getMessage());
		}
		return resultClinic;
	}

	@Override
	public Map<Disease, BigDecimal> diseaseLikelihoods(Patient patient) {
		Map<Disease, BigDecimal> diseaseList = new HashMap<Disease, BigDecimal>();
		if (patient != null) {
			HashSet<String> patientSymtoms = new HashSet<String>(
					patient.getSymptoms());

			for (Disease disease : diseases) {
				if (!disease.getSymptoms().isEmpty()) {

					HashSet<String> diseaseSymtoms = new HashSet<String>(
							disease.getSymptoms());
					BigDecimal noOfSymtom = new BigDecimal(
							diseaseSymtoms.size());

					diseaseSymtoms.retainAll(patientSymtoms);

					if (diseaseSymtoms.size() > 0) {

						BigDecimal probility = new BigDecimal(
								diseaseSymtoms.size()).divide(noOfSymtom, 2,
								RoundingMode.HALF_EVEN);

						diseaseList.put(disease, probility);
					} else {
						diseaseList.put(disease, new BigDecimal(0));
					}
				}
			}
		}
		return diseaseList;

	}

	@Override
	public Map<Medication, BigDecimal> medicationsForDisease(Patient patient,
			Disease disease) {
		BigDecimal cost = new BigDecimal(0);
		BigDecimal wildCost = new BigDecimal(0);
		BigDecimal mimCost = new BigDecimal(0);
		HashMap<Medication, BigDecimal> medicationForDisease = new HashMap<Medication, BigDecimal>();
		HashMap<Medication, BigDecimal> minCostMedicationForDisease = new HashMap<Medication, BigDecimal>();
		if (patient != null && disease != null
				&& !disease.getMedicationCombinations().isEmpty()) {
			List<Map<String, BigDecimal>> medication = disease
					.getMedicationCombinations();
			for (Map<String, BigDecimal> mediList : medication) {
				for (Map.Entry<String, BigDecimal> entry : mediList.entrySet()) {
					if (!patient.medicationAllergies().contains(entry.getKey())) {

						for (Medication medications : medications) {
							if (medications.getName() != null
									&& medications.getName().equals(
											entry.getKey())
									&& medications.getCostPerMg() != null
									&& patient.getWeight() != null) {

								cost = cost.add(medications.getCostPerMg()
										.multiply(
												patient.getWeight().multiply(
														entry.getValue())));

								medicationForDisease
										.put(medications, patient.getWeight()
												.multiply(entry.getValue()));

								break;
							}

						}

					} else {
						break;
					}
				}
				if (mimCost.compareTo(wildCost) == 0
						|| mimCost.compareTo(cost) > 1) {
					mimCost = cost;
					cost = new BigDecimal(0);
					minCostMedicationForDisease = medicationForDisease;
					medicationForDisease = new HashMap<Medication, BigDecimal>();
				} else {
					medicationForDisease = new HashMap<Medication, BigDecimal>();
				}
			}
		}
		return minCostMedicationForDisease;

	}

	@Override
	public TreatmentPlan treatmentPlanForPatient(Patient patient) {

		if (patient != null) {
			TreatmentPlan treatmentPlan = new TreatmentPlan();
			HashMap<Medication, BigDecimal> medication = new HashMap<Medication, BigDecimal>();
			treatmentPlan.setAgeYearPortion(ageInYears(patient));
			treatmentPlan.setAgeMonthPortion(ageInMonths(patient) % 12);
			treatmentPlan.setClinics(clinicsBasedOnAgeAndDiseases(patient));

			Map<Disease, BigDecimal> possibleDisease = diseaseLikelihoods(patient);
			for (Disease disease : diseases) {
				if (possibleDisease.get(disease).compareTo(diseasePossibility) >= 0) {
					Map<Medication, BigDecimal> medicationByDisease = medicationsForDisease(
							patient, disease);

					for (Entry<Medication, BigDecimal> entry : medicationByDisease
							.entrySet()) {
						if (medication.get(entry.getKey()) != null) {
							medication.put(
									entry.getKey(),
									medication.get(entry.getKey()).add(
											entry.getValue()));
						} else {
							medication.put(entry.getKey(), entry.getValue());
						}
					}
				}
			}
			treatmentPlan.setMedications(medication);
			return treatmentPlan;
		} else {
			throw new UnsupportedOperationException("Patient can't be NULL");
		}
	}

}
