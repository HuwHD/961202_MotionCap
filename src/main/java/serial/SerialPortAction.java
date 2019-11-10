/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial;

/**
 *
 * @author dev
 */
public interface SerialPortAction {
    boolean action(String s);
    void fail(Exception e);
}
