package org.example.templatejava6.chat.controller;

import org.example.templatejava6.chat.dto.ChatRequestDto;
import org.example.templatejava6.chat.dto.ChatResponseDto;
import org.example.templatejava6.chat.entity.PhienChatAi;
import org.example.templatejava6.chat.service.ChatAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatAiController {

    @Autowired
    private ChatAiService chatAiService;

    @PostMapping("/phien")
    public ResponseEntity<PhienChatAi> taoPhien(@RequestParam(required = false) Integer idKhachHang) {
        return ResponseEntity.ok(chatAiService.taoPhienMoi(idKhachHang));
    }

    @GetMapping("/phien/{idKhachHang}")
    public ResponseEntity<List<PhienChatAi>> getPhienCuaKhach(@PathVariable Integer idKhachHang) {
        return ResponseEntity.ok(chatAiService.getPhienCuaKhachHang(idKhachHang));
    }

    @GetMapping("/tin-nhan/{idPhien}")
    public ResponseEntity<List<ChatResponseDto>> getLichSuTinNhan(@PathVariable Integer idPhien) {
        return ResponseEntity.ok(chatAiService.getLichSuTinNhan(idPhien));
    }

    @PostMapping("/tin-nhan")
    public ResponseEntity<ChatResponseDto> guiTinNhan(@RequestBody ChatRequestDto request) {
        return ResponseEntity.ok(chatAiService.guiTinNhan(request));
    }
}
