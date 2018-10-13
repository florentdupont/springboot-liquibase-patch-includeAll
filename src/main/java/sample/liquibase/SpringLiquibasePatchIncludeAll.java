package sample.liquibase;

import liquibase.integration.spring.SpringLiquibase;

import java.io.IOException;
import java.util.Set;

import static java.util.stream.Collectors.toSet;


/**
 * Classe pour patcher le comportement en erreur des includeAll lorsque la configuration est externalisée (hors classpath).
 */
public class SpringLiquibasePatchIncludeAll extends SpringLiquibase {

    protected SpringResourceOpener createResourceOpener() {
        return new SpringResourceOpener(getChangeLog()) {

            @Override
            public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
                Set<String> result = super.list(relativeTo, path, includeFiles, includeDirectories, recursive);

                // un peu WTF le fait qu'il dégage les file: lors du includeAll ...
                // je les rajoute
                if(relativeTo.startsWith("file:"))
                    return result.stream().map(x -> "file:"+x).collect(toSet());
                else
                    return result;
            }
        };
    }
}
