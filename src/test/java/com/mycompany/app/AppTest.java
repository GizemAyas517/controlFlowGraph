package com.mycompany.app;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void Complex() {
        int x= 2;
        int y= 3;

        for(int a = 1 , b = 3; a < 4 && b < 4; a++, b++) {
            x++;
            y++;
        }

        while(x > 0 || y < 0) {
            x--;
        }
    }
}
