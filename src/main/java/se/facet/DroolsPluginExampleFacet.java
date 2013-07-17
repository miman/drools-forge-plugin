/**
 * 
 */
package se.facet;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import se.miman.forge.plugin.util.MimanBaseFacet;

/**
 * Adds the necessary dependencies and files to a project to run JBoss Drools rules.
 * 
 * - Adds dependencies to the pom file
 * 
 * - Creates a rules folder in the resources folder
 * 
 * - adds an drools-context.xml file to the resources folder
 * 
 * - adds an application context file to the resources folder
 * 
 * @author Mikael
 *
 */
@Alias("drools-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
      DependencyFacet.class })
public class DroolsPluginExampleFacet extends MimanBaseFacet
{

   public DroolsPluginExampleFacet()
   {
      super();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.forge.project.Facet#install()
    */
   @Override
   public boolean install()
   {
      configureProject();
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.forge.project.Facet#isInstalled()
    */
   @Override
   public boolean isInstalled()
   {
      final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
      Model pom = mvnFacet.getPOM();

      List<Dependency> deps = pom.getDependencies();
      boolean dependenciesOk = false;
      for (Dependency dependency : deps)
      {
         if (dependency.getGroupId().equals("org.slf4j") && dependency.getArtifactId().equals("slf4j-log4j12"))
         {
            dependenciesOk = true;
         }
         // TODO more checks should be added here
      }

      return dependenciesOk;
   }

   // Helper functions ****************************************
   /**
    * Configures the project to be a JBoss Forge plugin project.
    * Adds the necessary dependencies to the pom.xml file.
    * Creates the Forge.xml file
    */
   private void configureProject()
   {
      final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
      Model pom = mvnFacet.getPOM();

      mergePomFileWithTemplate(pom);
      mvnFacet.setPOM(pom);
   }

   @Override
   protected String getTargetPomFilePath()
   {
      return "/template-files/example/pom-example-fragment.xml";
   }
}
