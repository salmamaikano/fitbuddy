package app.fitbuddy.controller.crud;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.fitbuddy.FitBuddyApplication;
import app.fitbuddy.config.SecurityConfig;
import app.fitbuddy.dto.appuser.AppUserResponseDTO;
import app.fitbuddy.dto.exercise.ExerciseRequestDTO;
import app.fitbuddy.dto.exercise.ExerciseResponseDTO;
import app.fitbuddy.dto.exercise.ExerciseUpdateDTO;
import app.fitbuddy.service.crud.AppUserCrudService;
import app.fitbuddy.service.crud.ExerciseCrudService;

@WebMvcTest(ExerciseController.class)
@ContextConfiguration(classes = {FitBuddyApplication.class, SecurityConfig.class})
class ExerciseControllerTest {
	
	@Autowired	MockMvc mockMvc;
	@Autowired	ObjectMapper objectMapper;
	@MockBean	ExerciseCrudService exerciseCrudService;
	@MockBean	AppUserCrudService appUserCrudService;
	
	final String API_PATH = "/user/exercises";
	
	@Nested
	class Create {
		
		@Test
		@WithAnonymousUser
		void whenNotAuthed_shouldReturnRedirect302() throws Exception {
			ExerciseRequestDTO requestDTO = new ExerciseRequestDTO("exerciseName", null);
			
			mockMvc.perform(post(API_PATH)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDTO)))
			.andExpect(status().is(302));
		}
		
		@ParameterizedTest
		@ValueSource(strings = {"", "exerciseNameexerciseNameexerciseName"}) // <1 or >32
		@WithMockUser(authorities = {"USER", "ADMIN"})
		void whenExerciseNameSizeNotCorrect_shouldReturnBadRequest(String name) throws Exception {
			ExerciseRequestDTO requestDTO = new ExerciseRequestDTO(name, null);
			
			mockMvc.perform(post(API_PATH)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDTO)))
			.andExpect(status().isBadRequest());
		}
		
		@Test
		@WithMockUser(authorities = {"USER", "ADMIN"})
		void whenInputIsCorrect_shouldReturnOk() throws Exception {
			ExerciseRequestDTO requestDTO = new ExerciseRequestDTO("exerciseName", null);
			AppUserResponseDTO appUserResponseDTO = new AppUserResponseDTO(11, "name", "password", "roleName");
			
			when(appUserCrudService.readByName(anyString())).thenReturn(appUserResponseDTO);
			
			mockMvc.perform(post(API_PATH)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDTO)))
			.andExpect(status().isOk());
					
			verify(exerciseCrudService).create(any(ExerciseRequestDTO.class));
		}
	}
	
	@Nested
	class ReadAll {
		
		@Test
		@WithAnonymousUser
		void whenNotAuthed_shouldReturnRedirect302() throws Exception {
			mockMvc.perform(get(API_PATH)).andExpect(status().is(302));
		}
		
		@Test
		@WithMockUser(authorities = {"USER", "ADMIN"})
		void whenAuthed_shouldReturnExerciseDtoList() throws Exception {
			AppUserResponseDTO appUserResponseDTO = new AppUserResponseDTO(11, "name", "password", "roleName");
			ExerciseResponseDTO exerciseResponseDTO_1 = new ExerciseResponseDTO(1, "exerciseName", 11);
			ExerciseResponseDTO exerciseResponseDTO_2 = new ExerciseResponseDTO(2, "exerciseName", 11);
			List<ExerciseResponseDTO> exerciseResponseDTOs = List.of(exerciseResponseDTO_1, exerciseResponseDTO_2);
			
			when(appUserCrudService.readByName(anyString())).thenReturn(appUserResponseDTO);
			when(exerciseCrudService.readMany(anyInt())).thenReturn(exerciseResponseDTOs);
			
			MvcResult mvcResult = mockMvc.perform(get(API_PATH)).andExpect(status().isOk()).andReturn();
			
			List<ExerciseResponseDTO> actualExerciseResponseDTOs = objectMapper.readValue(
					mvcResult.getResponse().getContentAsString(),
					new TypeReference<List<ExerciseResponseDTO>>() {});
			
			assertEquals(exerciseResponseDTOs.size(), actualExerciseResponseDTOs.size());
			assertEquals(exerciseResponseDTOs.get(0), actualExerciseResponseDTOs.get(0));
			assertEquals(exerciseResponseDTOs.get(1), actualExerciseResponseDTOs.get(1));
		}		
	}
	
	@Nested
	class Update {
		
		@Test
		@WithAnonymousUser
		void whenNotAuthed_shouldReturnRedirect302() throws Exception {
			mockMvc.perform(put(API_PATH)).andExpect(status().is(302));
		}
		
		@Test
		@WithMockUser(authorities = {"USER", "ADMIN"})
		void whenPathVariableNotInteger_shouldReturnBadRequest() throws Exception {
			ExerciseUpdateDTO udpateDTO = new ExerciseUpdateDTO("exerciseName");
			
			mockMvc.perform(put(API_PATH + "/abc")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(udpateDTO)))
			.andExpect(status().isBadRequest());
		}
		
		@ParameterizedTest
		@ValueSource(strings = {"", "exerciseNameexerciseNameexerciseName"}) // <1 or >32
		@WithMockUser(authorities = {"USER", "ADMIN"})
		void whenExerciseNameSizeNotCorrect_shouldReturnBadRequest(String name) throws Exception {
			ExerciseUpdateDTO udpateDTO = new ExerciseUpdateDTO(name);
			
			mockMvc.perform(put(API_PATH + "/1")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(udpateDTO)))
			.andExpect(status().isBadRequest());
		}		
		
		@Test
		@WithMockUser(authorities = {"USER", "ADMIN"})
		void whenInputIsCorrect_shouldReturnOk() throws Exception {
			AppUserResponseDTO appUserResponseDTO = new AppUserResponseDTO(11, "name", "password", "roleName");
			ExerciseUpdateDTO udpateDTO = new ExerciseUpdateDTO("exerciseName");
			
			when(appUserCrudService.readByName(anyString())).thenReturn(appUserResponseDTO);
			
			mockMvc.perform(put(API_PATH + "/1")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(udpateDTO)))
			.andExpect(status().isOk());
			
			verify(exerciseCrudService).update(1, udpateDTO);	
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@WithAnonymousUser
		void whenNotAuthed_shouldReturnRedirect302() throws Exception {
			mockMvc.perform(delete(API_PATH)).andExpect(status().is(302));
		}
		
		@Test
		@WithMockUser(authorities = {"USER", "ADMIN"})
		void whenPathVariableNotInteger_shouldReturnBadRequest() throws Exception {
			mockMvc.perform(delete(API_PATH + "/abc")).andExpect(status().isBadRequest());
		}		
		
		@Test
		@WithMockUser(authorities = {"USER", "ADMIN"})
		void whenAppUserIdDoesntMatch_shouldReturnBadRequest() throws Exception {
			AppUserResponseDTO appUserResponseDTO = new AppUserResponseDTO(11, "name", "password", "roleName");
			ExerciseResponseDTO exerciseResponseDTO = new ExerciseResponseDTO(1, "exerciseName", 22);
			
			when(appUserCrudService.readByName(anyString())).thenReturn(appUserResponseDTO);
			when(exerciseCrudService.readById(anyInt())).thenReturn(exerciseResponseDTO);
			
			mockMvc.perform(delete(API_PATH + "/1")).andExpect(status().isBadRequest());
			
			verify(exerciseCrudService, times(0)).delete(anyInt());
		}
		
		@Test
		@WithMockUser(authorities = {"USER", "ADMIN"})
		void whenInputIsCorrect_shouldReturnOk() throws Exception {
			AppUserResponseDTO appUserResponseDTO = new AppUserResponseDTO(11, "name", "password", "roleName");
			ExerciseResponseDTO exerciseResponseDTO = new ExerciseResponseDTO(1, "exerciseName", 11);
			
			when(appUserCrudService.readByName(anyString())).thenReturn(appUserResponseDTO);
			when(exerciseCrudService.readById(anyInt())).thenReturn(exerciseResponseDTO);
			
			mockMvc.perform(delete(API_PATH + "/1")).andExpect(status().isOk());
			
			verify(exerciseCrudService).delete(1);
		}
	}
}
