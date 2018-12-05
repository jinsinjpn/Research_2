package subPopulationAvailability;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.modules.ChangeLegMode;
import org.matsim.core.replanning.modules.ReRoute;
import org.matsim.core.replanning.modules.TripsToLegsModule;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.vehicles.VehicleType;

public class ModeChoiceWithMultipleSubpopulation {

	    private static final String EQUIL_DIR = "C:/Users/MATSim/Desktop/FUKU-TEST/resources/";
	    private static final String PLANS_FILE = "C:/Users/MATSim/Desktop/FUKU-TEST/resources/population.xml";

	    private static final String SUBPOP_ATTRIB_NAME = "subpopulation";
	    private static final String SUBPOP1_NAME = "lower"; // half of persons will come in this group
	    private static final String SUBPOP2_NAME = "upper"; // rest half here. 

	    @Rule
	    public MatsimTestUtils helper = new MatsimTestUtils();

	    @Test
	    public void run() {
	        Config config = ConfigUtils.loadConfig(EQUIL_DIR + "/config.xml");
	        config.controler().setOutputDirectory(helper.getOutputDirectory());
	        config.plans().setInputFile(new File(PLANS_FILE).getAbsolutePath());

	        Scenario scenario = ScenarioUtils.loadScenario(config);

	        updateScenarioForMotorbikeAsMainMode(scenario); // these are things which are required to add another mode as main/network mode

	        createSubPopulationAttributes(scenario);

	        //  following is required to differentiate the agents
	        scenario.getConfig().plans().setSubpopulationAttributeName(SUBPOP_ATTRIB_NAME); /* This is the default anyway. */

	        // clear previous strategies
	        scenario.getConfig().strategy().clearStrategySettings();

	        // add innovative modules for SUBPOP1_NAME
	        {
	            StrategyConfigGroup.StrategySettings modeChoiceStrategySettings = new StrategyConfigGroup.StrategySettings() ;
	            modeChoiceStrategySettings.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ChangeTripMode.toString());
	            modeChoiceStrategySettings.setSubpopulation(SUBPOP1_NAME);
	            modeChoiceStrategySettings.setWeight(0.3);
	            scenario.getConfig().strategy().addStrategySettings(modeChoiceStrategySettings);

	            // a set of modes for first sub population
	            scenario.getConfig().changeMode().setModes(new String[] {"car", "bicycle"} );

	            StrategyConfigGroup.StrategySettings changeExpBetaStrategySettings = new StrategyConfigGroup.StrategySettings();
	            changeExpBetaStrategySettings.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
	            changeExpBetaStrategySettings.setSubpopulation(SUBPOP1_NAME);
	            changeExpBetaStrategySettings.setWeight(0.7);
	            scenario.getConfig().strategy().addStrategySettings(changeExpBetaStrategySettings);
	        }

	        // add innovative modules for SUBPOP1_NAME
	        {
	            StrategyConfigGroup.StrategySettings modeChoiceStrategySettings = new StrategyConfigGroup.StrategySettings() ;
	            modeChoiceStrategySettings.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ChangeTripMode.toString().concat(SUBPOP2_NAME)); // a different name is must. Amit June'17
	            modeChoiceStrategySettings.setSubpopulation(SUBPOP2_NAME);
	            modeChoiceStrategySettings.setWeight(0.3);
	            scenario.getConfig().strategy().addStrategySettings(modeChoiceStrategySettings);

	            StrategyConfigGroup.StrategySettings changeExpBetaStrategySettings = new StrategyConfigGroup.StrategySettings();
	            changeExpBetaStrategySettings.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
	            changeExpBetaStrategySettings.setSubpopulation(SUBPOP2_NAME);
	            changeExpBetaStrategySettings.setWeight(0.7);
	            scenario.getConfig().strategy().addStrategySettings(changeExpBetaStrategySettings);
	        }

	        // disable innovation
	        scenario.getConfig().strategy().setFractionOfIterationsToDisableInnovation(0.8);

	        Controler controler = new Controler(scenario);

	        // this is required to set the different set of available modes for second sub population example.
	        // (The name of the innovative module should be same as set in config.strategy())
	        controler.addOverridingModule(new AbstractModule() {
	            @Override
	            public void install() {
	                final Provider<TripRouter> tripRouterProvider = binder().getProvider(TripRouter.class);
	                addPlanStrategyBinding(DefaultPlanStrategiesModule.DefaultStrategy.ChangeTripMode.name().concat(SUBPOP2_NAME)).toProvider(new javax.inject.Provider<PlanStrategy>() {
	                    final String[] availableModes = {"car", "motorbike"};
	                    @Inject
	                    Scenario sc;

	                    @Override
	                    public PlanStrategy get() {
	                        final PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(new RandomPlanSelector<>());
	                        builder.addStrategyModule(new TripsToLegsModule(tripRouterProvider, sc.getConfig().global()));
	                        builder.addStrategyModule(new ChangeLegMode(sc.getConfig().global().getNumberOfThreads(), availableModes, true));
	                        builder.addStrategyModule(new ReRoute(sc, tripRouterProvider));
	                        return builder.build();
	                    }
	                });
	            }
	        });

	        controler.run();
	    }

	    private void createSubPopulationAttributes (final Scenario scenario) {
	        int totalPerson = scenario.getPopulation().getPersons().size();
	        // probably, with the newer population version, one can have attributes together with plans. Amit June'17
	        for (Id<Person> p : scenario.getPopulation().getPersons().keySet()) {
	                int personIdInteger = Integer.valueOf(p.toString());
	            if ( personIdInteger < totalPerson /2  ) {
	                scenario.getPopulation().getPersonAttributes().putAttribute(p.toString(), SUBPOP_ATTRIB_NAME, SUBPOP1_NAME);
	            } else {
	                scenario.getPopulation().getPersonAttributes().putAttribute(p.toString(), SUBPOP_ATTRIB_NAME, SUBPOP2_NAME);
	            }
	        }
	    }

	    private void updateScenarioForMotorbikeAsMainMode(final Scenario scenario) {
	        // add motorbike which is not already present in the scenario
	        Set<String> allowedMode = new HashSet<>();
	        allowedMode.add("car");
	        allowedMode.add("motorbike");
	        allowedMode.add("bicycle");

	        VehicleType motorbike = scenario.getVehicles().getFactory().createVehicleType(Id.create("motorbike",VehicleType.class));
	        motorbike.setMaximumVelocity(60/3.6);
	        motorbike.setPcuEquivalents(0.25);
	        scenario.getVehicles().addVehicleType(motorbike);

	        scenario.getConfig().plansCalcRoute().setNetworkModes(allowedMode);
	        scenario.getConfig().qsim().setMainModes(allowedMode);
	        scenario.getConfig().travelTimeCalculator().setAnalyzedModes("car,motorbike,bicycle");

	        scenario.getConfig().planCalcScore().getOrCreateModeParams("motorbike"); // this will set default scoring params for motorbike

	        for (Link l : scenario.getNetwork().getLinks().values()) {
	            l.setAllowedModes(allowedMode);
	        }
	    }
	}