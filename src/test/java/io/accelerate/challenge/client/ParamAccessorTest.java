package io.accelerate.challenge.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParamAccessorTest {
    
    // ~~~~~~~~~~~~~~~~~~~~~  Primitives  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    @Test
    void deserialize_integer() throws JsonProcessingException {
        assertThat(asParamAccessor("1").getAsInteger(), is(1));
    }

    @Test
    void deserialize_integer_throw_exception_if_string() {
        assertThrows(RuntimeException.class, () -> asParamAccessor("\"1\"").getAsInteger());
    }

    @Test
    void deserialize_string() throws JsonProcessingException {
        assertThat(asParamAccessor("\"someString\"").getAsString(), is("someString"));
    }

    @Test
    void deserialize_string_from_integer() throws JsonProcessingException {
        assertThat(asParamAccessor("1").getAsString(), is("1"));
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~  Lists  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    @Test
    void deserialize_list_of_int() throws JsonProcessingException {
        assertThat(asParamAccessor("[1, 2, 3]").getAsListOf(Integer.class), is(List.of(1, 2, 3)));
    }

    @Test
    void deserialize_list_of_string() throws JsonProcessingException {
        assertThat(asParamAccessor("[\"value1\", \"value2\", \"value3\"]").getAsListOf(String.class), is(List.of("value1", "value2", "value3")));
    }
    
    @Test
    void throws_exception_when_trying_to_deserialize_invalid_list()  {
        assertThrows(RuntimeException.class, () -> asParamAccessor("\"notAList\"").getAsListOf(Integer.class));
    }

    // ~~~~~~~~~~~~~~~~~~~~~  Maps  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    void deserialize_map_of_int() throws JsonProcessingException {
        assertThat(asParamAccessor("{\"key1\": 11, \"key2\": 22}").getAsMapOf(Integer.class), 
                is(Map.of("key1", 11, "key2", 22)));
    }

    @Test
    void deserialize_map_of_string() throws JsonProcessingException {
        assertThat(asParamAccessor("{\"key1\": \"text1\", \"key2\": \"text2\"}").getAsMapOf(String.class),
                is(Map.of("key1", "text1", "key2", "text2")));
    }

    @Test
    void throws_exception_when_trying_to_deserialize_invalid_map()  {
        assertThrows(RuntimeException.class, () -> asParamAccessor("\"notAList\"").getAsMapOf(Integer.class));
    }


    // ~~~~~~~~~~~~~~~~~~~~~  Objects  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    

    @Test
    void deserialize_class() throws JsonProcessingException {
        assertThat(asParamAccessor("{\"field1\": 11, \"field2\": \"value12\"}").getAsObject(SomeRecord.class), is(new SomeRecord(11, "value12")));
    }

    @Test
    void deserialize_as_generic_primitive() throws JsonProcessingException {
        assertThat(asParamAccessor("1").getAsObject(Object.class), is(1));
    }

    @Test
    void deserialize_as_generic_object() throws JsonProcessingException {
        assertThat(asParamAccessor("{\"field1\": 11, \"field2\": \"value12\"}").getAsObject(Object.class), is(Map.of("field1", 11, "field2", "value12")));
    }

    // ~~~~~~~~~~~~~~~~~~ Helpers ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private static ParamAccessor asParamAccessor(String number) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode element = objectMapper.readValue(number, new TypeReference<>() {});
        return new ParamAccessor(element);
    }
    
    private record SomeRecord(int field1, String field2) { }
}
