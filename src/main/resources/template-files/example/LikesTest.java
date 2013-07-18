package ${packageReplace};

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.drools.event.process.ProcessEventListener;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.WorkingMemoryEventListener;
import org.drools.runtime.StatelessKnowledgeSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class LikesTest {

    @Autowired
    StatelessKnowledgeSession productsKSession;
    
    /**
     * Test is not run in perfect isolation - the purpose is to show the outcome of processing with Drools
     * @throws Exception
     */
    @Test
    public void testLikes() throws Exception {
    	Likes veryLiked = new Likes();
    	veryLiked.setNoOfLikes(24);
    	Likes notSoLiked = new Likes();
    	notSoLiked.setNoOfLikes(9);
    	
    	List<Likes> likesList = new ArrayList<Likes>();
    	likesList.add(veryLiked);
    	likesList.add(notSoLiked);

        productsKSession.execute(likesList);

        assertEquals(0, veryLiked.getNoOfChecks());
        assertEquals(0, notSoLiked.getNoOfChecks());

        System.out.println(format("Very liked [likes: %s, checks %s]", veryLiked.getNoOfLikes(), veryLiked.getNoOfChecks()));
        System.out.println(format("Not so liked [likes: %s, checks %s]", notSoLiked.getNoOfLikes(), notSoLiked.getNoOfChecks()));
    }
}
