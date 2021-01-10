package com.enotes.note.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class Utils {

   public static ResultActions postRequest(MockMvc mvc, final Object ob, String mainPath, String path, ObjectMapper objectMapper ) throws Exception {
    return mvc.perform(post(PathBuilder.buildPath(mainPath, path))
        .content(asJsonString(objectMapper, ob))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));
  }

  public static String asJsonString(final ObjectMapper objectMapper, final Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T fromJson(ObjectMapper objectMapper,String string, Class<T> clazz)
      throws IOException {

    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper.readValue(string, clazz);
  }
}
