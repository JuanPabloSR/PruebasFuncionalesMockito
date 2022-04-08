package com.example.RESTfulTest.controller;

import com.example.RESTfulTest.model.Widget;
import com.example.RESTfulTest.service.WidgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WidgetRestControllerTest {

    @MockBean
    private WidgetService service;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("GET /widgets success")
    void testGetWidgetsSuccess() throws Exception {
        // Configurar el Mock
        Widget widget1 = new Widget(1l, "Widget Name", "Description", 1);
        Widget widget2 = new Widget(2l, "Widget 2 Name", "Description 2", 4);
        doReturn(Lists.newArrayList(widget1, widget2)).when(service).findAll();

        // Ejecutando la solicitud GET
        mockMvc.perform(get("/rest/widgets"))
                // Valida el codigo de respuesta y el contenido
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Valida los headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widgets"))

                // Valida el retorno de los datos
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Widget Name")))
                .andExpect(jsonPath("$[0].description", is("Description")))
                .andExpect(jsonPath("$[0].version", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Widget 2 Name")))
                .andExpect(jsonPath("$[1].description", is("Description 2")))
                .andExpect(jsonPath("$[1].version", is(4)));
    }


    @Test
    @DisplayName("GET /rest/widget/1 - Not Found")
    void testGetWidgetByIdNotFound() throws Exception {
        // Configurar el Mock
        doReturn(Optional.empty()).when(service).findById(1l);

        // Ejecutando la solicitud GET
        mockMvc.perform(get("/rest/widget/{id}", 1L))
                // Valida el codigo de respuesta
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("POST /rest/widget")
    void testCreateWidget() throws Exception {
        // Configurar el Mock
        Widget widgetToPost = new Widget("New Widget", "This is my widget");
        Widget widgetToReturn = new Widget(1L, "New Widget", "This is my widget", 1);
        doReturn(widgetToReturn).when(service).save(any());

        // Ejecutando la solicitud POST
        mockMvc.perform(post("/rest/widget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(widgetToPost)))

                // Valida el codigo de respuesta y el contenido
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validar headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))

                // Valida el retorno de los datos
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Widget")))
                .andExpect(jsonPath("$.description", is("This is my widget")))
                .andExpect(jsonPath("$.version", is(1)));
    }


    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Test para modificar por Id
    @Test
    @DisplayName("PUT /rest/widget/1")
    void testUpdateNotFoundWidget() throws Exception {
        // Configurar el Mock
        Widget widget = new Widget("New widget", "This is a Widget");
        Widget widgetById = new Widget(1L, "New widget", "This is a widget", 2);
        Widget widgetSave = new Widget(1L, "New widget", "This is a widget", 3);
        Mockito.doReturn(Optional.of(widgetById)).when(service).findById(1L);
        Mockito.doReturn(widgetSave).when(service).save(Mockito.any());

        // Ejecutando la solicitud PUT
        mockMvc.perform(MockMvcRequestBuilders.put("/rest/widget/{id}", 21)

                        // Valida  el contenido
                        .contentType(MediaType.APPLICATION_JSON)
                        // Validar headers
                        .header(HttpHeaders.IF_MATCH, 2)
                        .content(asJsonString((widget))))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // Test para buscar por id
    @Test
    @DisplayName("GET /rest/widget/1 byId")
    void testGetWidgetById() throws Exception {
        // Configurar el Mock
        Widget widgetById = new Widget(1L, "New Widget", "This is a Widget", 2);
        Mockito.doReturn(Optional.of(widgetById)).when(service).findById(1L);

        // Ejecutando la solicitud POST
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/widget/{id}", 1))
                // Valida el codigo de respuesta y el contenido
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))

                // Validar headers
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ETAG, "\"2\""))

                // Valida el retorno de los datos
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is("New Widget")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is("This is a Widget")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.version", CoreMatchers.is(2)));
    }


}