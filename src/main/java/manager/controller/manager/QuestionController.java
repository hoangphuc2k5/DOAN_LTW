package manager.controller.manager;

import manager.entity.ImageAttachment;
import manager.entity.Question;
import manager.service.ImageService;
import manager.service.QuestionService;
import manager.service.ActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/manager/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ImageService imageService;
    private final ActivityLogService logService;

    public QuestionController(QuestionService questionService,
                              ImageService imageService,
                              ActivityLogService logService) {
        this.questionService = questionService;
        this.imageService = imageService;
        this.logService = logService;
    }

    // 🔹 Danh sách câu hỏi đã duyệt
    @GetMapping
    @Transactional(readOnly = true)
    public String listApprovedQuestions(Model model) {
        List<Question> questions = questionService.findAllApproved();
        model.addAttribute("title", "Câu hỏi đã duyệt");
        model.addAttribute("isPending", false);
        model.addAttribute("questions", questions);
        return "manager/question/list";
    }

    // 🔹 Danh sách câu hỏi chưa duyệt
    @GetMapping("/pending")
    @Transactional(readOnly = true)
    public String listPendingQuestions(Model model) {
        List<Question> pending = questionService.findAllPending();
        model.addAttribute("title", "Câu hỏi chờ duyệt");
        model.addAttribute("isPending", true);
        model.addAttribute("questions", pending);
        return "manager/question/list";
    }

    // 🔹 Xem chi tiết 1 câu hỏi
    @GetMapping("/{id}")
    public String viewQuestion(@PathVariable Long id, Model model) {
        Question q = questionService.findById(id);
        List<ImageAttachment> images = imageService.getImagesByQuestion(id);

        model.addAttribute("question", q);
        model.addAttribute("images", images);
        model.addAttribute("isApproved", q.getIsApproved());
        return "manager/question/detail";
    }

    // 🔹 Duyệt bài viết
    @PostMapping("/{id}/approve")
    public String approveQuestion(@PathVariable Long id) {
        questionService.approve(id);
        logService.logAction(1L, "APPROVE_QUESTION", "QUESTION", id, "Approved question " + id);
        return "redirect:/manager/questions/" + id + "?approved";
    }

    // 🔹 Từ chối bài viết
    @PostMapping("/{id}/reject")
    public String rejectQuestion(@PathVariable Long id) {
        questionService.reject(id);
        logService.logAction(1L, "REJECT_QUESTION", "QUESTION", id, "Rejected question " + id);
        return "redirect:/manager/questions/" + id + "?rejected";
    }

    // 🔹 Upload ảnh cho câu hỏi
    @PostMapping("/{id}/upload")
    public String uploadQuestionImage(@PathVariable Long id,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam(name = "uploaderId", required = false) Long uploaderId) {
        if (uploaderId == null) uploaderId = 1L;
        imageService.uploadForQuestion(file, id, uploaderId);
        logService.logAction(uploaderId, "UPLOAD_QUESTION_IMAGE", "IMAGE", id, "Uploaded image for question " + id);
        return "redirect:/manager/questions/" + id + "?uploaded";
    }
}
