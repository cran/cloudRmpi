/*
    Copyright 2011 Northbranchlogic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package nbl.utilj;

import java.awt.*;
import javax.swing.*;

    /** Static methods for finding and manipulating the sizes of
     *  gui components.
     */
public class GuiMetrics {

        // ----------- Constants ------------------------------------

    public static int STANDARD_SPACE = 5;

        // ------------ static variables -----------------------------

    public static int screenWidth;
    public static int screenHeight;
    public static FontMetrics fontMetrics;

        /** The width in pixels of 'X', in the default font of a
         *  <tt>JLabel</tt>.
         */
    public static int widthX;
        /** The height of a line in the the default font of a
         *  <tt>JLabel</tt>.
         */
    public static int lineHeight;

          /** Used as sample <tt>JComponent</tt> for finding the size of
	    things. */
    public static JLabel LABEL_SAMPLE;

        /** Flag to prevent init from being called in non-gui
         *  applications. TEMPORARY KLUDGE.
         */
    public static boolean permitInit = true;

    private static boolean isInitialized = false;

        // ----------- init ----------------------------------------

            /* NOTE: this method should NOT be run using
             * <tt>SwingUtilities.invokeLater()</tt>
             * as <tt>GuiSizeConstants.init()</tt> depends on its having
             *  already been run.
             */
    public static void init() {

        if ( !permitInit ) return;
        if ( isInitialized ) return;

        LABEL_SAMPLE = new JLabel("Abc");
        screenWidth = getScreenWidth();
        screenHeight = getScreenHeight();
        fontMetrics = LABEL_SAMPLE.getFontMetrics(LABEL_SAMPLE.getFont());
        widthX = fontMetrics.charWidth('X');
        lineHeight = fontMetrics.getHeight();
        isInitialized = true;
    }

            // ---------- Screen size methods -----------------------

    public static int getScreenWidth(JComponent c) {
	return( getScreenSize(c).width );
    }

    public static int getScreenWidth() {
	return( getScreenWidth(LABEL_SAMPLE) );
    }

    public static int getScreenHeight(JComponent c) {
	return( getScreenSize(c).height );
    }

    public static int getScreenHeight() {
	return( getScreenHeight(LABEL_SAMPLE) );
    }

    public static Dimension getScreenSize(JComponent c) {
        try {
        return(c.getToolkit().getScreenSize());
        }
        catch(NullPointerException xxx) {
            System.err.println("c == null? " + (c == null));
            throw(xxx);
        }
    }

    public static Dimension getScreenSize() {
        return(getScreenSize(LABEL_SAMPLE));
    }

        // -------- Size calculations -------------------------

    public static Dimension noBiggerThanScreen(Dimension d) {
        return( new Dimension( Math.min(d.width,screenWidth),
                               Math.min(d.height,screenHeight) ) );
    }

    public static Dimension noBiggerThanScreenFraction(Dimension d,
                                                       float fraction) {
        return( new Dimension( Math.min(d.width,
                                        roundMult(screenWidth,fraction)),
                               Math.min(d.height,
                                        roundMult(screenHeight,fraction)) ) );
    }

    public static int roundMult(int v,float factor) {
	return((int) Math.round(((float) v) * factor));
    }

        /** Calculates a fraction of the screen size.  It never returns
         *  zero values; if a zero is calculated, it is promoted to unity.
         */
    public static Dimension screenFraction(float fraction) {
        return( new Dimension( Math.max(roundMult(screenWidth,fraction),1),
                               Math.max(roundMult(screenHeight,fraction),1) ) );
    }

        /** The width, in pixels, of a fraction of the screeen width.
         */
    public static int toPixelWidth(float fraction) {
        return( roundMult(screenWidth,fraction) );
    }

        /** The height, in pixels, of a fraction of the screeen height.
         */
    public static int toPixelHeight(float fraction) {
        return( roundMult(screenHeight,fraction) );
    }

    public static int roundDiv(int numer,int denom) {
        return((int) Math.round(((float) numer)/((float) denom)));
    }

        // ----------- Widths ------------------------------------

    public static int getWidth(String s) {
        return( fontMetrics.stringWidth(s) );
    }

        // ----------- Heights ------------------------------------

        /** Converts a number of pixels to an exact multiple of a
         *  another integer, rounding up.
         *
         *  @param h the number to modified.
         *  @param lineH the 'base', which the return will be a multiple of.
         */
    public static int upToMultiple(int h,int lineH) {
        if ( (h % lineH) == 0 ) return(h);  // It's already perfect
        else return( ((int) Math.ceil(((double) h)/((double) lineH)))
                     * lineH );
    }
}