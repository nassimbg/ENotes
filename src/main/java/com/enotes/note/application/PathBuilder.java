package com.enotes.note.application;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PathBuilder {

  public static final String AUTHENTICATION = "authentication";

  private PathBuilder() {
    //do nothing
  }

  public static String buildPath(Object... pathParams) {
    return Stream.of(pathParams)
        .map(Object::toString)
        .collect(Collectors.joining("/", "/", ""));
  }
}
