package ar.com.fernandospr.wns;

import ar.com.fernandospr.wns.model.*;
import ar.com.fernandospr.wns.model.builders.WnsTileBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class WnsTileBuilderTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public WnsTileBuilderTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( WnsTileBuilderTest.class );
    }

    /**
     * @brief test builder with multiple bindings
     */
    //@Test
    public void testBuild()
    {
    	WnsTileBuilder builder = new WnsTileBuilder()
    		.bindingTemplateTileSquareBlock("text1", "text2")
    		.addBinding()
    		.bindingTemplateTileSquareBlock("text3", "text4");

    	WnsTile res = builder.build();

    	assertEquals( 2, res.visual.bindings.size());
    	WnsBinding elt0 = res.visual.bindings.get(0);
    	assertEquals( 2, elt0.texts.size());
    	assertEquals( "text1", elt0.texts.get(0).value);
    	assertEquals( "text2", elt0.texts.get(1).value);
    }
}
