package net.ikwa.techconnect.controllers;

import net.ikwa.techconnect.service.TechCreatorReg;
import net.ikwa.techconnect.userregDTO.CreatorRegDTO;
import net.ikwa.techconnect.userregDTO.CreatorRegDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/creators")
@CrossOrigin("*")
@ResponseBody
public class TechCreatorController {

    @Autowired
    private TechCreatorReg creatorService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreatorRegDTO dto) {
        try {
            creatorService.registerCreator(dto);
            return ResponseEntity.ok("Creator registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getCreator(@PathVariable String email) {
        try {
            CreatorRegDTO dto = creatorService.getCreatorDetails(email);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Creator not found");
        }
    }

    @PutMapping("/{email}/image")
    public ResponseEntity<?> updateImage(@PathVariable String email, @RequestBody String image) {
        try {
            creatorService.updateProfileImage(email, image);
            return ResponseEntity.ok("Image updated");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Update failed");
        }
    }

    @DeleteMapping("/{email}/image")
    public ResponseEntity<?> deleteImage(@PathVariable String email) {
        try {
            creatorService.deleteProfileImage(email);
            return ResponseEntity.ok("Image deleted");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Delete failed");
        }
    }
}
