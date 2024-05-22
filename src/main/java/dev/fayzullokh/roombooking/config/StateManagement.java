package dev.fayzullokh.roombooking.config;

import dev.fayzullokh.roombooking.enums.State;
import jakarta.inject.Singleton;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component
public class StateManagement {

    private final Map<Long, State> adminState = new HashMap<>();
}
