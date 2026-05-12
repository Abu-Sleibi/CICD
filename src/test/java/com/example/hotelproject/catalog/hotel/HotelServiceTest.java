package com.example.hotelproject.catalog.hotel;

import com.example.hotelproject.catalog.room.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel hotel;
    private HotelRequestDto hotelRequest;
    private HotelRequestDto hotelRequestWithDifferentEmail;
    private HotelResponseDto hotelResponse;
    private RoomType roomType;
    private RoomTypeRequestDto roomTypeRequest;
    private RoomTypeResponseDto roomTypeResponse;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Hotel");
        hotel.setDescription("Luxury hotel in city center");
        hotel.setAddress("123 Main Street");
        hotel.setCity("Amman");
        hotel.setCountry("Jordan");
        hotel.setPhone("+96261234567");
        hotel.setEmail("info@grandhotel.com");
        hotel.setStarRating(5);
        hotel.setActive(true);
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());

        hotelRequest = new HotelRequestDto();
        hotelRequest.setName("Grand Hotel");
        hotelRequest.setDescription("Luxury hotel in city center");
        hotelRequest.setAddress("123 Main Street");
        hotelRequest.setCity("Amman");
        hotelRequest.setCountry("Jordan");
        hotelRequest.setPhone("+96261234567");
        hotelRequest.setEmail("info@grandhotel.com");
        hotelRequest.setStarRating(5);

        hotelRequestWithDifferentEmail = new HotelRequestDto();
        hotelRequestWithDifferentEmail.setName("Grand Hotel");
        hotelRequestWithDifferentEmail.setDescription("Luxury hotel in city center");
        hotelRequestWithDifferentEmail.setAddress("123 Main Street");
        hotelRequestWithDifferentEmail.setCity("Amman");
        hotelRequestWithDifferentEmail.setCountry("Jordan");
        hotelRequestWithDifferentEmail.setPhone("+96261234567");
        hotelRequestWithDifferentEmail.setEmail("newemail@hotel.com");
        hotelRequestWithDifferentEmail.setStarRating(5);

        hotelResponse = mock(HotelResponseDto.class);

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe Room");
        roomType.setDescription("Spacious room");
        roomType.setCapacity(2);
        roomType.setBasePrice(new BigDecimal("150.00"));
        roomType.setTotalRooms(20);
        roomType.setHotel(hotel);
        roomType.setActive(true);

        roomTypeRequest = new RoomTypeRequestDto();
        roomTypeRequest.setName("Deluxe Room");
        roomTypeRequest.setDescription("Spacious room");
        roomTypeRequest.setCapacity(2);
        roomTypeRequest.setBasePrice(new BigDecimal("150.00"));
        roomTypeRequest.setTotalRooms(20);
        roomTypeRequest.setHotelId(1L);

        roomTypeResponse = mock(RoomTypeResponseDto.class);

        lenient().when(hotelResponse.getId()).thenReturn(1L);
        lenient().when(hotelResponse.getName()).thenReturn("Grand Hotel");
        lenient().when(hotelResponse.getCity()).thenReturn("Amman");
        lenient().when(hotelResponse.getEmail()).thenReturn("info@grandhotel.com");

        lenient().when(roomTypeResponse.getId()).thenReturn(1L);
        lenient().when(roomTypeResponse.getName()).thenReturn("Deluxe Room");
        lenient().when(roomTypeResponse.getHotelId()).thenReturn(1L);
        lenient().when(roomTypeResponse.getCapacity()).thenReturn(2);
        lenient().when(roomTypeResponse.getBasePrice()).thenReturn(new BigDecimal("150.00"));
        lenient().when(roomTypeResponse.getHotelName()).thenReturn("Grand Hotel");
    }

    @Test
    void createHotel_Success() {
        when(hotelRepository.existsByNameAndCity("Grand Hotel", "Amman")).thenReturn(false);
        when(hotelRepository.existsByEmail("info@grandhotel.com")).thenReturn(false);
        when(hotelMapper.toEntity(hotelRequest)).thenReturn(hotel);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);
        when(hotelMapper.toDto(hotel)).thenReturn(hotelResponse);

        HotelResponseDto result = hotelService.createHotel(hotelRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(hotelRepository).save(any(Hotel.class));
    }

    @Test
    void createHotel_ThrowsException_WhenNameAndCityExist() {
        when(hotelRepository.existsByNameAndCity("Grand Hotel", "Amman")).thenReturn(true);

        assertThatThrownBy(() -> hotelService.createHotel(hotelRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(hotelRepository, never()).save(any(Hotel.class));
    }

    @Test
    void createHotel_ThrowsException_WhenEmailExists() {
        when(hotelRepository.existsByNameAndCity("Grand Hotel", "Amman")).thenReturn(false);
        when(hotelRepository.existsByEmail("info@grandhotel.com")).thenReturn(true);

        assertThatThrownBy(() -> hotelService.createHotel(hotelRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(hotelRepository, never()).save(any(Hotel.class));
    }

    @Test
    void getHotelById_Success() {
        when(hotelRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(hotel));
        when(hotelMapper.toDto(hotel)).thenReturn(hotelResponse);

        HotelResponseDto result = hotelService.getHotelById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getHotelById_ThrowsException_WhenNotFound() {
        when(hotelRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.getHotelById(99L))
                .isInstanceOf(HotelNotFoundException.class)
                .hasMessageContaining("Could not find hotel 99");
    }

    @Test
    void getAllHotels_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hotel> hotelPage = new PageImpl<>(Arrays.asList(hotel));

        when(hotelRepository.findAllByActiveTrue(pageable)).thenReturn(hotelPage);
        when(hotelMapper.toDto(hotel)).thenReturn(hotelResponse);

        Page<HotelResponseDto> result = hotelService.getAllHotels(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateHotel_Success() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.existsByEmailAndIdNot("newemail@hotel.com", 1L)).thenReturn(false);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);
        when(hotelMapper.toDto(hotel)).thenReturn(hotelResponse);

        HotelResponseDto result = hotelService.updateHotel(1L, hotelRequestWithDifferentEmail);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(hotelRepository).existsByEmailAndIdNot("newemail@hotel.com", 1L);
        verify(hotelRepository).save(hotel);
    }

    @Test
    void updateHotel_ThrowsException_WhenEmailExists() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.existsByEmailAndIdNot("newemail@hotel.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> hotelService.updateHotel(1L, hotelRequestWithDifferentEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(hotelRepository, never()).save(any(Hotel.class));
    }

    @Test
    void deleteHotel_Success() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotelId(1L)).thenReturn(Arrays.asList(roomType));

        hotelService.deleteHotel(1L);

        assertThat(hotel.isActive()).isFalse();
        assertThat(roomType.isActive()).isFalse();
        verify(hotelRepository).save(hotel);
        verify(roomTypeRepository).saveAll(anyList());
    }

    @Test
    void activateHotel_Success() {
        hotel.setActive(false);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        hotelService.activateHotel(1L);

        assertThat(hotel.isActive()).isTrue();
        verify(hotelRepository).save(hotel);
    }

    @Test
    void createRoomType_Success() {
        when(hotelRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.existsByNameAndHotelId("Deluxe Room", 1L)).thenReturn(false);
        when(hotelMapper.toEntity(roomTypeRequest)).thenReturn(roomType);
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);
        when(hotelMapper.toRoomTypeDto(roomType)).thenReturn(roomTypeResponse);

        RoomTypeResponseDto result = hotelService.createRoomType(roomTypeRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(roomTypeRepository).save(any(RoomType.class));
    }

    @Test
    void createRoomType_ThrowsException_WhenHotelNotFound() {
        when(hotelRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.createRoomType(roomTypeRequest))
                .isInstanceOf(HotelNotFoundException.class);
    }

    @Test
    void createRoomType_ThrowsException_WhenNameExists() {
        when(hotelRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.existsByNameAndHotelId("Deluxe Room", 1L)).thenReturn(true);

        assertThatThrownBy(() -> hotelService.createRoomType(roomTypeRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(roomTypeRepository, never()).save(any(RoomType.class));
    }

    @Test
    void getRoomTypeById_Success() {
        when(roomTypeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(roomType));
        when(hotelMapper.toRoomTypeDto(roomType)).thenReturn(roomTypeResponse);

        RoomTypeResponseDto result = hotelService.getRoomTypeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getRoomTypeById_ThrowsException_WhenNotFound() {
        when(roomTypeRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.getRoomTypeById(99L))
                .isInstanceOf(RoomTypeNotFoundException.class);
    }

    @Test
    void getRoomTypesByHotelId_Success() {
        when(roomTypeRepository.findByHotelIdAndActiveTrue(1L)).thenReturn(Arrays.asList(roomType));
        when(hotelMapper.toRoomTypeDto(roomType)).thenReturn(roomTypeResponse);

        List<RoomTypeResponseDto> result = hotelService.getRoomTypesByHotelId(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    void getRoomTypesByHotelId_ReturnsEmptyList_WhenNoRoomTypes() {
        when(roomTypeRepository.findByHotelIdAndActiveTrue(1L)).thenReturn(Arrays.asList());

        List<RoomTypeResponseDto> result = hotelService.getRoomTypesByHotelId(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void updateRoomType_Success() {
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);
        when(hotelMapper.toRoomTypeDto(roomType)).thenReturn(roomTypeResponse);

        RoomTypeResponseDto result = hotelService.updateRoomType(1L, roomTypeRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(roomTypeRepository).save(roomType);
    }

    @Test
    void updateRoomType_WithDifferentName_Success() {
        roomTypeRequest.setName("New Room Name");

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(roomTypeRepository.existsByNameAndHotelIdAndIdNot("New Room Name", 1L, 1L))
                .thenReturn(false);
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);
        when(hotelMapper.toRoomTypeDto(roomType)).thenReturn(roomTypeResponse);

        RoomTypeResponseDto result = hotelService.updateRoomType(1L, roomTypeRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(roomTypeRepository).save(roomType);
    }

    @Test
    void updateRoomType_ThrowsException_WhenNameExists() {
        roomTypeRequest.setName("Existing Room Name");

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(roomTypeRepository.existsByNameAndHotelIdAndIdNot("Existing Room Name", 1L, 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> hotelService.updateRoomType(1L, roomTypeRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(roomTypeRepository, never()).save(any(RoomType.class));
    }

    @Test
    void updateRoomType_WithDifferentHotel_Success() {
        Hotel newHotel = new Hotel();
        newHotel.setId(2L);
        roomTypeRequest.setHotelId(2L);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(hotelRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(newHotel));
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);
        when(hotelMapper.toRoomTypeDto(roomType)).thenReturn(roomTypeResponse);

        RoomTypeResponseDto result = hotelService.updateRoomType(1L, roomTypeRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(roomTypeRepository).save(roomType);
    }

    @Test
    void updateRoomType_WithDifferentHotel_ThrowsException_WhenHotelNotFound() {
        roomTypeRequest.setHotelId(99L);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(hotelRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.updateRoomType(1L, roomTypeRequest))
                .isInstanceOf(HotelNotFoundException.class);

        verify(roomTypeRepository, never()).save(any(RoomType.class));
    }

    @Test
    void deleteRoomType_Success() {
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        hotelService.deleteRoomType(1L);

        assertThat(roomType.isActive()).isFalse();
        verify(roomTypeRepository).save(roomType);
    }

    @Test
    void deleteRoomType_ThrowsException_WhenNotFound() {
        when(roomTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.deleteRoomType(99L))
                .isInstanceOf(RoomTypeNotFoundException.class);

        verify(roomTypeRepository, never()).save(any(RoomType.class));
    }

    @Test
    void searchHotels_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hotel> hotelPage = new PageImpl<>(Arrays.asList(hotel));

        when(hotelRepository.searchHotels(eq("Amman"), eq("Jordan"), eq(4), any(Pageable.class)))
                .thenReturn(hotelPage);
        when(hotelMapper.toDto(hotel)).thenReturn(hotelResponse);

        Page<HotelResponseDto> result = hotelService.searchHotels("Amman", "Jordan", 4, null, null, null, null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getHotelsByCity_Success() {
        when(hotelRepository.findByCityAndActiveTrue("Amman")).thenReturn(Arrays.asList(hotel));
        when(hotelMapper.toDto(hotel)).thenReturn(hotelResponse);

        List<HotelResponseDto> result = hotelService.getHotelsByCity("Amman");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    void getHotelsByCountry_Success() {
        when(hotelRepository.findByCountryAndActiveTrue("Jordan")).thenReturn(Arrays.asList(hotel));
        when(hotelMapper.toDto(hotel)).thenReturn(hotelResponse);

        List<HotelResponseDto> result = hotelService.getHotelsByCountry("Jordan");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    void getHotelsByRating_Success() {
        when(hotelRepository.findByStarRatingAndActiveTrue(5)).thenReturn(Arrays.asList(hotel));
        when(hotelMapper.toDto(hotel)).thenReturn(hotelResponse);

        List<HotelResponseDto> result = hotelService.getHotelsByRating(5);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }
}