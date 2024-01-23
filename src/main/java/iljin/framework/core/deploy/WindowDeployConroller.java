package iljin.framework.core.deploy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class WindowDeployConroller {
    private final WindowDeployService windowDeployService;

    @Autowired
    public WindowDeployConroller(WindowDeployService windowDeployService) {
        this.windowDeployService = windowDeployService;
    }

    @GetMapping("/deploy")
    public ResponseEntity<String> deploy() {
        return windowDeployService.getCallDeployService();
    }
}
