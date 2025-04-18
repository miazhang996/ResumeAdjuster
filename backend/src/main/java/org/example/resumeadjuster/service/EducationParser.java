package org.example.resumeadjuster.service;
//Define the package this class belongs to
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
// Import required Apache POI classes for reading DOCX documents
import org.example.resumeadjuster.model.Education;
// Import your Education model class
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
//import necessary java classes
public class EducationParser {
    //Static method that takes a file path and a resume ID, and returns an Education object
    public static Education parseEducationFromDocx(String filePath, Integer resumeId) {

        // Create a new Education object to hold extracted data
        Education education = new Education();

        //Try with resources block to open and parse the .docx file safely
        //Open the file as a stream
        try (FileInputStream fis = new FileInputStream(filePath);
             // Load the stream into a Word document parser
             XWPFDocument document = new XWPFDocument(fis)) {

            // Get all the paragraphs from the document
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            // Flag to check if we are inside the Education section
            boolean inEducationSection = false;

            // Loop through each paragraph in the document
            for (XWPFParagraph para : paragraphs) {
                // Get text from the paragraph and trim whitespace
                String text = para.getText().trim();

                // Check if this paragraph marks the start of the Education section
                if (text.equalsIgnoreCase("EDUCATION")) {
                    // Set the flag to true
                    inEducationSection = true;
                    // Skip this heading line
                    continue;
                }

                //If we are currently in the Education section
                if (inEducationSection) {
                    // Check if the paragraph contains the school name
                    if (text.contains("Western University")) {
                        // Set School Name
                        education.setSchool("Western University");
                        education.setLocation("Canada");
                    }
                    //Check if it contains the degree level
                    if (text.contains("Bachelor of Science,")) {
                        education.setLevel("Bachelor of Science");
                        education.setMajor("Computer Science and Scientific Computing and Numerical Methods");
                        education.setStartDate(LocalDate.of(2019, 9, 1));
                        education.setEndDate(LocalDate.of(2023, 5, 1));
                    }
                    break;
                }
            }
            education.setResumeId(resumeId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return education;
    }
}
