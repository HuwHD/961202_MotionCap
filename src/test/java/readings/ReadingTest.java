package readings;

import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import serial.Reading;

public class ReadingTest {

    @Test
        @Ignore
    public void testRangeDouble() {
        Reading r;
        r = Reading.parse("10.456,143,0.5,.5,20,0,0:");
        assertEquals("Reading{x=10.456, y=143.0, NS=0.5, WE=0.5, h=20.0, la=0.0 b1=false, b2=false}", r.toString());

        r = Reading.parse("10.456,143,-0.5,-0.5,20,1,0:");
        assertEquals("Reading{x=10.456, y=143.0, NS=-0.5, WE=-0.5, h=20.0, la=0.0 b1=true, b2=false}", r.toString());

        r = Reading.parse("10.456,143.99,-2,-16,20,1,0:");
        assertEquals("Reading{x=10.456, y=143.99, NS=-2.0, WE=-16.0, h=20.0, la=0.0 b1=true, b2=false}", r.toString());

        r = Reading.parse("10.456,143,0,-.55,20,1,0,");
        assertEquals("Reading{x=10.456, y=143.0, NS=0.0, WE=-0.55, h=20.0, la=0.0 b1=true, b2=false}", r.toString());

        r = Reading.parse("10.456,143,0,0,20.5,1,1,");
        assertEquals("Reading{x=10.456, y=143.0, NS=0.0, WE=0.0, h=20.5, la=0.0 b1=true, b2=true}", r.toString());

        r = Reading.parse("10.456,143,0,0,20,1,1:");
        assertEquals("Reading{x=10.456, y=143.0, NS=0.0, WE=0.0, h=20.0, la=0.0 b1=true, b2=true}", r.toString());

        r = Reading.parse("10.456,143,-2,-16,20.5,1:");
        assertNull(r);

        r = Reading.parse("10.456,143,-2a,-16,1,0:");
        assertNull(r);

        r = Reading.parse(",143,-2,-16,1,0:");
        assertNull(r);

        r = Reading.parse(",,143,-2,-16,1,0:");
        assertNull(r);
    }

    @Test
    @Ignore
    public void testRangeInt() {
        Reading r;
        r = Reading.parse("10,143,1,1,20,0,0:");
        assertEquals("Reading{x=10.0, y=143.0, NS=1.0, WE=1.0, h=20.0, la=0.0 b1=false, b2=false}", r.toString());

        r = Reading.parse("10,143,0,1,20,1,0:");
        assertEquals("Reading{x=10.0, y=143.0, NS=0.0, WE=1.0, h=20.0, la=0.0 b1=true, b2=false}", r.toString());

        r = Reading.parse("10,143,-2,-16,20,1,0:");
        assertEquals("Reading{x=10.0, y=143.0, NS=-2.0, WE=-16.0, h=20.0, la=0.0 b1=true, b2=false}", r.toString());

        r = Reading.parse("10,143,0,0,20,1,0,");
        assertEquals("Reading{x=10.0, y=143.0, NS=0.0, WE=0.0, h=20.0, la=0.0 b1=true, b2=false}", r.toString());

        r = Reading.parse("10,143,0,0,20,1,1,");
        assertEquals("Reading{x=10.0, y=143.0, NS=0.0, WE=0.0, h=20.0, la=0.0 b1=true, b2=true}", r.toString());

        r = Reading.parse("10,143,0,0,20,1,1:");
        assertEquals("Reading{x=10.0, y=143.0, NS=0.0, WE=0.0, h=20.0, la=0.0 b1=true, b2=true}", r.toString());

        r = Reading.parse("10,143,-2,-16,20,1:");
        assertNull(r);

        r = Reading.parse("10,143,-2a,-16,20,1,0:");
        assertNull(r);

        r = Reading.parse(",143,-2,-16,1,0:");
        assertNull(r);

        r = Reading.parse(",,143,-2,-16,1,0:");
        assertNull(r);

    }
}
