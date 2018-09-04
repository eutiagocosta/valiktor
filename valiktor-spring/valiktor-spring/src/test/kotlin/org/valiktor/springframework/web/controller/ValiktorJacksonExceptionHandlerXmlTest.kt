package org.valiktor.springframework.web.controller

import org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE
import org.springframework.http.HttpHeaders.LOCATION
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.log
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Locale
import kotlin.test.Test

class ValiktorJacksonExceptionHandlerXmlTest {

    private val mockMvc = ValiktorExceptionHandlerFixture.mockMvc
    private val xml = ValiktorExceptionHandlerFixture.XML

    @Test
    fun `should return 201`() {
        mockMvc
            .perform(post("/employees")
                .accept(APPLICATION_XML)
                .contentType(APPLICATION_XML)
                .content(xml.payloadEmployeeValid()))
            .andExpect(status().isCreated)
            .andExpect(header().string(LOCATION, "http://localhost/employees/1"))
            .andExpect(content().bytes(ByteArray(0)))
            .andDo(log())
    }

    @Test
    fun `should return 422 with locale en`() {
        mockMvc
            .perform(post("/employees")
                .accept(APPLICATION_XML)
                .header(ACCEPT_LANGUAGE, "en")
                .contentType(APPLICATION_XML)
                .content(xml.payloadEmployeeNullName()))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_XML))
            .andExpect(content().xml(xml.payload422NullName(Locale.ENGLISH)))
            .andDo(log())
    }

    @Test
    fun `should return 422 with locale pt_BR`() {
        mockMvc
            .perform(post("/employees")
                .accept(APPLICATION_XML)
                .header(ACCEPT_LANGUAGE, "pt-BR")
                .contentType(APPLICATION_XML)
                .content(xml.payloadEmployeeNullName()))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_XML))
            .andExpect(content().xml(xml.payload422NullName(Locale("pt", "BR"))))
            .andDo(log())
    }
}