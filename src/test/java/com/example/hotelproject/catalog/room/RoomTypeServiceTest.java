package com.example.hotelproject.catalog.room;

import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.hotel.HotelNotFoundException;
import com.example.hotelproject.catalog.hotel.HotelRepository;
import com.example.hotelproject.catalog.hotel.HotelServiceImpl;
import com.example.hotelproject.catalog.hotel.HotelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class RoomTypeServiceTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private RoomType roomType;
    private RoomTypeRequestDto roomTypeRequest;
    private RoomTypeResponseDto roomTypeResponse;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Hotel");
        hotel.setActive(true);

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe Room");
        roomType.setDescription("Spacious room with city view");
        roomType.setCapacity(2);
        roomType.setBasePrice(new BigDecimal("150.00"));
        roomType.setTotalRooms(20);
        roomType.setHotel(hotel);
        roomType.setActive(true);

        roomTypeRequest = new RoomTypeRequestDto();
        roomTypeRequest.setName("Deluxe Room");
        roomTypeRequest.setDescription("Spacious room with city view");
        roomTypeRequest.setCapacity(2);
        roomTypeRequest.setBasePrice(new BigDecimal("150.00"));
        roomTypeRequest.setTotalRooms(20);
        roomTypeRequest.setHotelId(1L);

        roomTypeResponse = mock(RoomTypeResponseDto.class);

        lenient().when(roomTypeResponse.getId()).thenReturn(1L);
        lenient().when(roomTypeResponse.getName()).thenReturn("Deluxe Room");
        lenient().when(roomTypeResponse.getCapacity()).thenReturn(2);
        lenient().when(roomTypeResponse.getBasePrice()).thenReturn(new BigDecimal("150.00"));
        lenient().when(roomTypeResponse.getHotelId()).thenReturn(1L);
        lenient().when(roomTypeResponse.getHotelName()).thenReturn("Grand Hotel");
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
        assertThat(result.getName()).isEqualTo("Deluxe Room");
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
        assertThat(result.get(0).getId()).isEqualTo(1L);
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
        newHotel.setName("New Hotel");
        newHotel.setActive(true);
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
}