package org.example.dto.chat;

import org.example.aop.MessageType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class MessageTypeDeserializer extends JsonDeserializer<MessageType> {
    @Override
    public MessageType deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        String value = jsonParser.getText();
        return MessageType.valueOf(value.toUpperCase()); // Convert to uppercase to match enum
    }
}
