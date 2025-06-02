package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

public class TestPerformance {

	/*
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * 
	 * These tests can be modified to suit new solutions, just as long as the
	 * performance metrics at the end of the tests remains consistent.
	 * 
	 * These are performance metrics that we are trying to hit:
	 * 
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */
	private GpsUtil gpsUtil;
	private RewardsService rewardsService;
	private TourGuideService tourGuideService;

	// Nombre de threads = nombre de processeurs * 4 pour optimiser le parallélisme
	private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;
	// Nombre total d'utilisateurs pour le test
	private static final int USER_COUNT = 100;
	// Taille des lots pour le traitement par lots
	private static final int BATCH_SIZE = USER_COUNT / 10;
	// Fréquence des logs pour le suivi de progression
	private static final int LOG_FREQUENCY = BATCH_SIZE / 10;
	// Service exécuteur pour gérer le pool de threads
	private ExecutorService rewardsExecutor;
	// Service exécuteur pour gérer le pool de threads
	private ExecutorService trackExecutor;

	/**
	 * Configuration initiale avant chaque test
	 * Initialise les services et configure le nombre d'utilisateurs
	 */
	@BeforeEach
	public void setUp() {
		gpsUtil = new GpsUtil();
		rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(USER_COUNT);
		tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		// Initialisation du pool de threads pour le suivi de localisation
		trackExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		// Initialisation du pool de threads pour les récompenses
		rewardsExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

	}

	/**
	 * Test de performance pour le suivi de localisation
	 * Objectif : Traiter 100 000 utilisateurs en moins de 15 minutes
	 */
	@Test
	public void highVolumeTrackLocation() {

		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes

		List<User> allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		// Compteur atomique pour suivre le progrès du traitement
		AtomicInteger totalProcessed = new AtomicInteger(0);

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < allUsers.size(); i += BATCH_SIZE) {

			int end = Math.min(i + BATCH_SIZE, allUsers.size());
			List<User> batch = allUsers.subList(i, end);

			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

				batch.forEach(user -> {

					tourGuideService.trackUserLocation(user);
					int count = totalProcessed.incrementAndGet();
					System.out.println("Utilisateurs traités: " + count + "/" + allUsers.size() +
							" - " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

				});
			}, trackExecutor);

			futures.add(future);
		}
		// Attendre que tous les futurs soient terminés
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	/**
	 * Test de performance pour le calcul des récompenses
	 * Objectif : Calculer les récompenses pour 100 000 utilisateurs en moins de 20
	 * minutes
	 */
	@Test
	public void highVolumeGetRewards() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		// Pré-calcul des attractions une seule fois
		List<Attraction> attractions = gpsUtil.getAttractions();
		Attraction attraction = attractions.get(0);
		List<User> allUsers = tourGuideService.getAllUsers();
		VisitedLocation commonLocation = new VisitedLocation(allUsers.get(0).getUserId(), attraction, new Date());

		// Optimisation de l'ajout des locations
		allUsers.parallelStream().forEach(u -> u.addToVisitedLocations(commonLocation));

		System.out.println("\nDébut du calcul des récompenses - Lots: " + BATCH_SIZE);
		AtomicInteger totalProcessed = new AtomicInteger(0);

		List<CompletableFuture<Void>> futures = IntStream.range(0, (allUsers.size() + BATCH_SIZE - 1) / BATCH_SIZE)
				.mapToObj(i -> {
					int start = i * BATCH_SIZE;
					int end = Math.min(start + BATCH_SIZE, allUsers.size());
					return CompletableFuture.runAsync(() -> {
						allUsers.subList(start, end).parallelStream().forEach(user -> {
							rewardsService.calculateRewards(user);

							// Compteur atomique pour suivre le progrès du traitement
							int count = totalProcessed.incrementAndGet();
							if (count % LOG_FREQUENCY == 0) {
								System.out.println("Progrès: " + count + "/" + allUsers.size());
							}
						});
					}, rewardsExecutor);
				})
				.collect(Collectors.toList());

		// Attendre que tous les futurs soient terminés
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		long rewardsCount = allUsers.parallelStream()
				.filter(user -> user.getUserRewards().size() > 0)
				.count();

		assertTrue(rewardsCount == allUsers.size());

		System.out
				.println("highVolumeGetRewards: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
