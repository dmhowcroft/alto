/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.laboratory;

/**
 * Thrown when a variable in a task is not given in the execution of said task. 
 * @author Jonas
 * @see <a href = "https://bitbucket.org/tclup/alto/wiki/AltoLab">
 * bitbucket.org/tclup/alto/wiki/AltoLab</a>
 */
public class VariableNotDefinedException extends Exception {
    
    public VariableNotDefinedException(String variableName, String line) {
        super("Variable " +variableName + " in line " + line + " is undefined in this program and was not found in the variable remapper");
    }
    
    public VariableNotDefinedException(String variableName) {
        super("Variable " +variableName + " is undefined in this program and was not found in the variable remapper");
    }
    
}
