/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.logging.Level;
import my.com.tmrnd.tmforce.common.db.DatabaseSingleton;
import my.com.tmrnd.tmforce.db.config.DbConfiguration;
import my.com.tmrnd.tmforce.nff.assignment.db.DatabaseService;
import org.glassfish.jersey.logging.LoggingFeature;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alam
 */
public class AutoAssignMain extends Application<AutoAssignConfig> {

    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    private final HibernateBundle<DbConfiguration> hibernateBundle = DatabaseSingleton.getBundle();

    @Override
    public void initialize(Bootstrap<AutoAssignConfig> bootstrap) {
        bootstrap.addBundle(hibernateBundle);
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor()
                )
        );

    }

    @Override
    public void run(AutoAssignConfig config, Environment environment) throws Exception {

        DatabaseService senderDatabaseService = new UnitOfWorkAwareProxyFactory(hibernateBundle).create(DatabaseService.class, SessionFactory.class, hibernateBundle.getSessionFactory());
        DatabaseService.setDatabaseService(senderDatabaseService);
        AutoAssignConfig.setPreAssignConfig(config);

        environment.healthChecks().register("healthcheck", new AutoAssignHealthCheck());

        if (config.isLogPayload()) {
            log.info("logPayload: true. Payloads will be printed in log");
            environment.jersey().register(new LoggingFeature(java.util.logging.Logger.getLogger(
                    LoggingFeature.class.getName()),
                    Level.INFO,
                    LoggingFeature.Verbosity.PAYLOAD_TEXT,
                    8192));
        } else {
            log.info("logPayload: false. Payloads will not be printed in log");
        }

        log.info("MatrixDaemon is enabled");
        AutoAssignDaemon autoAssignDaemon = new AutoAssignDaemon();
        autoAssignDaemon.startMatrixService();

    }

    public static void main(String[] args) throws Exception {
        new AutoAssignMain().run(args);
    }
}
