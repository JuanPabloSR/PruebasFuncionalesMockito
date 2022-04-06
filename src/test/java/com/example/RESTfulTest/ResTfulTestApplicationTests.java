package com.example.RESTfulTest;

import com.example.RESTfulTest.model.Widget;
import com.example.RESTfulTest.service.WidgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
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
		Widget widget1 = new Widget(1l, "Widget Name", "Description", 1);
		Widget widget2 = new Widget(2l, "Widget 2 Name", "Description 2", 4);
		doReturn(Lists.newArrayList(widget1, widget2)).when(service).findAll();

		mockMvc.perform(MockMvcRequestBuilders.get("/rest/widgets"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/rest/widgets"))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", CoreMatchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", CoreMatchers.is("Widget Name")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", CoreMatchers.is("Description")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].version", CoreMatchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].id", CoreMatchers.is(2)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].name", CoreMatchers.is("Widget 2 Name")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].description", CoreMatchers.is("Description 2")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].version", CoreMatchers.is(4)));
	}

	@Test
	@DisplayName("GET /rest/widget/1 - Not Found")
	void testGetWidgetByIdNotFound() throws Exception {
		Mockito.doReturn(Optional.empty()).when(service).findById(1l);

		mockMvc.perform(MockMvcRequestBuilders.get("/rest/widget/{id}", 1L))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	@DisplayName("POST /rest/widget")
	void testCreateWidget() throws Exception {
		Widget widgetToPost = new Widget("New Widget", "This is my widget");
		Widget widgetToReturn = new Widget(1L, "New Widget", "This is my widget", 1);
		Mockito.doReturn(widgetToReturn).when(service).save(Mockito.any());

		mockMvc.perform(MockMvcRequestBuilders.post("/rest/widget")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJsonString(widgetToPost)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
				.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ETAG, "\"1\""))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is("New Widget")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is("This is my widget")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.version", CoreMatchers.is(1)));
	}
	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("PUT /rest/widget/1")
	void testUpdateNotFoundWidget() throws Exception {
		Widget widget = new Widget("New widget", "This is a Widget");
		Widget widgetById = new Widget(1L, "New widget", "This is a widget", 2);
		Widget widgetSave = new Widget(1L, "New widget", "This is a widget", 3);
		Mockito.doReturn(Optional.of(widgetById)).when(service).findById(1L);
		Mockito.doReturn(widgetSave).when(service).save(Mockito.any());

		mockMvc.perform(MockMvcRequestBuilders.put("/rest/widget/{id}", 21)
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.IF_MATCH, 2)
						.content(asJsonString((widget))))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	@DisplayName("GET /rest/widget/1 byId")
	void testGetWidgetById() throws Exception{

		Widget widgetById = new Widget(1L, "New Widget", "This is a Widget", 2);
		Mockito.doReturn(Optional.of(widgetById)).when(service).findById(1L);

		mockMvc.perform(MockMvcRequestBuilders.get("/rest/widget/{id}", 1))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))

				.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
				.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ETAG, "\"2\""))

				.andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is("New Widget")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is("This is a Widget")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.version", CoreMatchers.is(2)));
	}

}