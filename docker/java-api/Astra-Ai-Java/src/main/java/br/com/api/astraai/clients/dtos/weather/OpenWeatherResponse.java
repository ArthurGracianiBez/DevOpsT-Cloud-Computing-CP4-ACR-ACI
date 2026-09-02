package br.com.api.astraai.clients.dtos.weather;

import java.util.List;

public record OpenWeatherResponse(
        List<WeatherInfo> weather
) {}