package de.tuberlin.amos.ws17.swit.transform;

import java.util.Iterator;
import java.util.LinkedList;

public class TransformationBuffer {
  public TransformationBuffer() {
    this(10.0);
  }

  public TransformationBuffer(double cacheDuration) {
    this.cacheDuration = cacheDuration;

    transformations = new LinkedList<TransformStamped>();
  }

  public boolean insert(TransformStamped t) {
    if(transformations.isEmpty()) {
      transformations.addLast(t);
      return true;
    }

    if(t.getStamp() > transformations.getLast().getStamp()) {
      transformations.addLast(t);

      // remove old transformations until cacheDuration is not exceeded
      while(transformations.getLast().getStamp() - transformations.getFirst().getStamp() > cacheDuration * 1000) {
        transformations.removeFirst();
      }

      return true;
    }

    return false;
  }

  public TransformStamped lookup(long stamp) {
    if(transformations.isEmpty()) {
      throw new TransformBufferException("No transformation data available!");
    }

    if(stamp > transformations.getLast().getStamp()) {
      if(transformations.size() < 2) {
        throw new TransformBufferException("Cannot extrapolate into the future. Not enough data!");
      }

      TransformStamped last = transformations.getLast();
      TransformStamped lastButOne = transformations.get(transformations.size()-2);
      double t = (double)(stamp - lastButOne.getStamp()) / (double)(last.getStamp() - lastButOne.getStamp());

      return new TransformStamped(stamp, TransformInterpolation.extrapolate(lastButOne, last, t));
    }

    if(stamp < transformations.getFirst().getStamp()) {
      throw new TransformBufferException("Not designed to extrapolate into the past!");
    }

    Iterator it = transformations.iterator();

    for (int i = 0; i < transformations.size(); i++) {
      TransformStamped upper = transformations.get(i);

      if(upper.getStamp() == stamp) {
        return transformations.get(i);
      }

      if(upper.getStamp() > stamp) {
        assert i > 0;
        TransformStamped lower = transformations.get(i-1);

        double t = (double)(stamp - lower.getStamp()) / (double)(upper.getStamp() - lower.getStamp());

        return new TransformStamped(stamp, TransformInterpolation.interpolate(lower, upper, t));
      }
    }

    throw new TransformBufferException("Unexpected failure!");
  }

  private double cacheDuration;

  private LinkedList<TransformStamped> transformations;
}
