/**
 * 
 */
package eu.miman.forge.plugin.drools.facet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import eu.miman.forge.plugin.util.MimanBaseFacet;
import eu.miman.forge.plugin.util.VelocityUtil;
import eu.miman.forge.plugin.util.helpers.DomFileHelper;
import eu.miman.forge.plugin.util.helpers.DomFileHelperImpl;

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
public class DroolsPluginFacet extends MimanBaseFacet
{

   DomFileHelper domFileHelper;

   private final VelocityEngine velocityEngine;
   private VelocityUtil velocityUtil;

   public DroolsPluginFacet()
   {
      super();
      domFileHelper = new DomFileHelperImpl();

      velocityUtil = new VelocityUtil();

      velocityEngine = new VelocityEngine();
      velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,
            "classpath");
      velocityEngine.setProperty("classpath.resource.loader.class",
            ClasspathResourceLoader.class.getName());
      velocityEngine.setProperty(
            RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
            "org.apache.velocity.runtime.log.JdkLogChute");
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
         if (dependency.getGroupId().equals("org.drools") && dependency.getArtifactId().equals("drools-spring"))
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

      createDroolsContextFile();
      createRulesFile(pom.getGroupId());
      mergeApplicationContextFile(pom.getProjectDirectory().getAbsolutePath());
   }

   /**
    * Merges the applicationContext.xml template with the existing one.
    * 
    * If no xml file exists, we create one
    */
	private void mergeApplicationContextFile(String prjAbsolutePath) {
		String sourceUri = "/template-files/resources/applicationContext-rules-fragment.xml";
		String targetUri = "src/main/resources/applicationContext.xml";

		try {
			Xpp3Dom dom = domFileHelper.readXmlResourceFile(sourceUri);
			String targetPath = prjAbsolutePath + "/" + targetUri;
			Xpp3Dom targetDom = domFileHelper.readXmlFile(targetPath);

			Xpp3Dom resultingDom = Xpp3DomUtils.mergeXpp3Dom(targetDom, dom,
					false);
			domFileHelper.writeXmlFile(targetPath, resultingDom);
		} catch (FileNotFoundException e) {
			// The application context doesn't exist -> use fill xml file template
			sourceUri = "/template-files/resources/applicationContext-rules-full.xml";
			targetUri = "applicationContext.xml";
			
			final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
			Model pom = mvnFacet.getPOM();
			Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
			velocityPlaceholderMap.put("packageReplace", pom.getGroupId());
			
			VelocityContext velocityContext = velocityUtil
					.createVelocityContext(velocityPlaceholderMap);
			velocityUtil.createResourceAbsolute(sourceUri, velocityContext,
					targetUri, project, velocityEngine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

   /**
    * Creates the forge.xml.
    * We need this to be able to have a dependency to the miman-forge-plugin-util-impl project.
    */
   private void createDroolsContextFile()
   {
      String sourceUri = "/template-files/resources/drools-context.xml";
      String targetUri = "drools-context.xml";

      Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();

      VelocityContext velocityContext = velocityUtil
            .createVelocityContext(velocityPlaceholderMap);
      velocityUtil.createResourceAbsolute(sourceUri, velocityContext, targetUri, project, velocityEngine);
   }
   
   /**
    * Creates the forge.xml.
    * We need this to be able to have a dependency to the miman-forge-plugin-util-impl project.
    */
   private void createRulesFile(String grpId)
   {
      String sourceUri = "/template-files/resources/rules/rules.drl";
      String targetUri = "rules/rules.drl";

      Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
      velocityPlaceholderMap.put("packageReplace", grpId);

      VelocityContext velocityContext = velocityUtil
            .createVelocityContext(velocityPlaceholderMap);
      velocityUtil.createResourceAbsolute(sourceUri, velocityContext, targetUri, project, velocityEngine);
   }
   
   @Override
   protected String getTargetPomFilePath()
   {
      return "/template-files/pom.xml";
   }
}
