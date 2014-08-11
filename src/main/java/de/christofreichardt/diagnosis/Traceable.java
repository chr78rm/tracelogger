/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

/**
 * Classes which implement this interface delegate the retrieval of a particular tracer to the
 * specified method. This can be used to override the configuration of the {@link TracerFactory} for
 * certain classes.
 * 
 * @author Christof Reichardt
 */
public interface Traceable {
  AbstractTracer getCurrentTracer();
}
