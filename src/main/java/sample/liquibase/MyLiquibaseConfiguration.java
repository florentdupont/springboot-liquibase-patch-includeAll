package sample.liquibase;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

@EnableConfigurationProperties({DataSourceProperties.class, LiquibaseProperties.class})
// est ce que ce conf n'est pas déjà d'office tiré?
@Import(MyLiquibaseConfiguration.LiquibaseJpaDependencyConfiguration.class)
@Configuration
public class MyLiquibaseConfiguration {

    private LiquibaseProperties properties;

    private DataSourceProperties dataSourceProperties;

    private ResourceLoader resourceLoader;

    private DataSource dataSource;

    private DataSource liquibaseDataSource;

    public MyLiquibaseConfiguration(LiquibaseProperties properties,
                                    DataSourceProperties dataSourceProperties, ResourceLoader resourceLoader,
                                    ObjectProvider<DataSource> dataSource,
                                    @LiquibaseDataSource ObjectProvider<DataSource> liquibaseDataSource) {

        this.properties = properties;
        this.dataSourceProperties = dataSourceProperties;
        this.resourceLoader = resourceLoader;
        this.dataSource = dataSource.getIfUnique();
        this.liquibaseDataSource = liquibaseDataSource.getIfAvailable();
    }


    // je peux me créer un bean liquibase,
    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = createSpringLiquibase();
        liquibase.setChangeLog(this.properties.getChangeLog());
        liquibase.setContexts(this.properties.getContexts());
        liquibase.setDefaultSchema(this.properties.getDefaultSchema());
        liquibase.setDropFirst(this.properties.isDropFirst());
        liquibase.setShouldRun(this.properties.isEnabled());
        liquibase.setLabels(this.properties.getLabels());
        liquibase.setChangeLogParameters(this.properties.getParameters());
        liquibase.setRollbackFile(this.properties.getRollbackFile());

        return liquibase;
    }

    private SpringLiquibase createSpringLiquibase() {
        DataSource liquibaseDataSource = getDataSource();
        SpringLiquibase liquibase = new SpringLiquibasePatchIncludeAll();
        liquibase.setDataSource(liquibaseDataSource);
        return liquibase;
    }

    private DataSource getDataSource() {
        if (this.liquibaseDataSource != null) {
            return this.liquibaseDataSource;
        }
        if (this.properties.getUrl() == null && this.properties.getUser() == null) {
            return this.dataSource;
        }
        return null;
    }

    // seulement dans le cas JPA
    @Configuration
    public static class LiquibaseJpaDependencyConfiguration
            extends EntityManagerFactoryDependsOnPostProcessor {

        public LiquibaseJpaDependencyConfiguration() {
            super("liquibase");
        }

    }

}