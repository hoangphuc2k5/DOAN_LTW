package com.stackoverflow.controller.user;

import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.Tag;
import com.stackoverflow.service.common.QuestionService;
import com.stackoverflow.service.common.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private QuestionService questionService;

    @GetMapping("/tags")
    public String listTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "36") int size,
            @RequestParam(defaultValue = "popular") String sort,
            @RequestParam(required = false) String search,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Tag> tags;
        
        if (search != null && !search.isEmpty()) {
            tags = tagService.searchTags(search, pageable);
            model.addAttribute("searchQuery", search);
        } else if ("name".equals(sort)) {
            tags = tagService.getTagsByName(pageable);
        } else {
            tags = tagService.getTagsByPopularity(pageable);
        }
        
        model.addAttribute("tags", tags);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tags.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("pageTitle", "Tags - Stack Overflow Clone");
        
        return "tags/list";
    }

    @GetMapping("/tags/{tagName}")
    public String viewTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {
        
        Tag tag = tagService.findByName(tagName)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Question> questions = questionService.getQuestionsByTag(tag, pageable);
        
        model.addAttribute("tag", tag);
        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questions.getTotalPages());
        model.addAttribute("pageTitle", "'" + tag.getName() + "' Questions - Stack Overflow Clone");
        
        return "tags/view";
    }
}

