package se;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

import se.facet.DroolsPluginExampleFacet;
import se.facet.DroolsPluginFacet;
import se.miman.forge.plugin.util.VelocityUtil;
import se.miman.forge.plugin.util.helpers.DomFileHelper;
import se.miman.forge.plugin.util.helpers.DomFileHelperImpl;

@Alias("drools")
@Help("A plugin that adds the necessary dependencies and files to a project to run JBoss Drools rules")
@RequiresProject
public class DroolsPluginPlugin implements Plugin
{
   @Inject
   private Event<InstallFacets> event;

   @Inject
   private Project project;

   @Inject
   private ShellPrintWriter writer;
   
   DomFileHelper domFileHelper;
   
	/**
	 * The velocity engine used to replace data in the supplied templates with the correct info.
	 */
	private final VelocityEngine velocityEngine;
	private VelocityUtil velocityUtil;
   
	public DroolsPluginPlugin() {
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

   @SetupCommand
   @Command(value = "setup", help = "Adds the necessary dependencies and files to a project to run JBoss Drools rules")
   public void setup(PipeOut out)
   {

      if (!project.hasFacet(ResourceFacet.class))
      {
         event.fire(new InstallFacets(ResourceFacet.class));
      }

      if (!project.hasFacet(DroolsPluginFacet.class))
         event.fire(new InstallFacets(DroolsPluginFacet.class));
      else
         ShellMessages.info(out, "Project is already a DroolsPlugin plugin project.");
   }

   /**
    * Command to create an example rule to the project.
    * 
    * It also adds an example DTO & a test JUnit class.
    * 
    * @param name	The wanted name of the route
    * @param path	Where the route should be created (under java/src)
    * @param out	Error info statements are written to this pipe to be displayed to the user
    */
	@Command(value = "add-example-rule", help = "Adds an example Drools rule to this project")
	public void addRoute(@Option(name = "name", required = true) String name,
			@Option(name = "path", required = false) String path, PipeOut out) {
		// Make sure
		if (!project.hasFacet(DroolsPluginExampleFacet.class)) {
			event.fire(new InstallFacets(DroolsPluginExampleFacet.class));
		}
		createExampleRuleDtoClass();
		createExampleRuleTestClass();
		createExampleLog4jFile();
		createExampleRulesFile();
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		mergeDroolsContextFile(pom.getProjectDirectory().getAbsolutePath());
	}

	/**
	 * Creates a DTO to be used in the example route
	 */
	private void createExampleRuleDtoClass() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		String parentPomUri = "/template-files/example/Likes.java";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("packageReplace", pom.getGroupId() + ".rules");
		
	    // Replace the current pom with the copied/merged
		VelocityContext velocityContext = velocityUtil.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createJavaSource(parentPomUri, velocityContext, project, velocityEngine);
	}

	/**
* Creates a Test class for the example route 
	 */
	private void createExampleRuleTestClass() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		String parentPomUri = "/template-files/example/LikesTest.java";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("packageReplace", pom.getGroupId() + ".rules");
		
	    // Replace the current pom with the copied/merged
		VelocityContext velocityContext = velocityUtil.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createJavaTestSource(parentPomUri, velocityContext, project, velocityEngine);
	}

	/**
	 * Creates the forge.xml. We need this to be able to have a dependency to
	 * the miman-forge-plugin-util-impl project.
	 */
	private void createExampleLog4jFile() {
		String sourceUri = "/template-files/example/log4j.xml";
		String targetUri = "log4j.xml";

		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("packageReplace", pom.getGroupId());

		VelocityContext velocityContext = velocityUtil
				.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createResourceAbsolute(sourceUri, velocityContext, targetUri, project, velocityEngine);
	}

	/**
	 * Creates the forge.xml. We need this to be able to have a dependency to
	 * the miman-forge-plugin-util-impl project.
	 */
	private void createExampleRulesFile() {
		String sourceUri = "/template-files/example/example_rules.drl";
		String targetUri = "rules/example_rules.drl";

		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("packageReplace", pom.getGroupId());

		VelocityContext velocityContext = velocityUtil
				.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createResourceAbsolute(sourceUri, velocityContext,
				targetUri, project, velocityEngine);
	}

	/**
	 * Add the example_rules drl file to the drools config file.
	 * 
	 *     <drools:kbase id="productsKBase">
	 *         <drools:resources>
	 *              <drools:resource type="DRL" source="classpath:rules/example_rules.drl"/>
	 *         </drools:resources>
	 *     </drools:kbase>
	 *                                     
	 * @param prjAbsolutePath
	 */
	private void mergeDroolsContextFile(String prjAbsolutePath) {
		String targetUri = "src/main/resources/drools-context.xml";

		try {
			String targetPath = prjAbsolutePath + "/" + targetUri;
			Xpp3Dom targetDom = domFileHelper.readXmlFile(targetPath);

			Xpp3Dom kbaseElem = targetDom.getChild("drools:kbase");
			if (kbaseElem != null) {
				Xpp3Dom resourcesElem = kbaseElem.getChild("drools:resources");
				if (resourcesElem != null) {
					Xpp3Dom resourceElem = new Xpp3Dom("drools:resource");
					resourceElem.setAttribute("type", "DRL");
					resourceElem.setAttribute("source", "classpath:rules/example_rules.drl");
					resourcesElem.addChild(resourceElem);
				} else {
					ShellMessages.warn(writer, "No drools:resources element can be found in the drools-context.xml file, the example_rules.drl file reference cannot be added !");
				}
			} else {
				ShellMessages.warn(writer, "No drools:kbase element can be found in the drools-context.xml file, the example_rules.drl file reference cannot be added !");
			}
			
			domFileHelper.writeXmlFile(targetPath, targetDom);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
