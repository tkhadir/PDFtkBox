/* ====================================================================
  Copyright 2017 Quanticate Ltd

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
==================================================================== */
package com.quanticate.opensource.pdftkbox;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

/**
 * Helper for using Apache PDFBox to fetch / set bookmarks
 */
public class Bookmarks implements Closeable {
   public static final String BookmarkBegin = "BookmarkBegin";
   public static final String BookmarkTitle = "BookmarkTitle";
   public static final String BookmarkLevel = "BookmarkLevel";
   public static final String BookmarkPageNumber = "BookmarkPageNumber";
   public static final String BookmarkZoom = "BookmarkZoom";
   public static final String BookmarkYOffset = "BookmarkYOffset";
   
   private PDDocument document;
   public Bookmarks(File pdf) throws IOException {
      document = PDDocument.load(pdf);
   }
   
   /**
    * Returns the Bookmarks of a PDF, in PDFtk-like format, or
    *  null if none are contained in the file
    */
   public String getBookmarks() throws IOException {
      PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();
      if (outline == null) return null;
      
      StringBuilder bm = new StringBuilder();
      exportBookmark(bm, outline, 1);
      return bm.toString();
   }
   protected void exportBookmark(StringBuilder bm, PDOutlineNode bookmark, int level) throws IOException {
      PDOutlineItem current = bookmark.getFirstChild();
      while (current != null) {
         PDDestination dest = null;

         // Check for a bookmark via an action
         if (current.getAction() != null) {
            PDAction action = current.getAction();
            if (action instanceof PDActionGoTo) {
               dest = ((PDActionGoTo)action).getDestination();
            }
         }
         if (dest == null) {
            dest = current.getDestination();
         }
         
         if (dest != null) {
            bm.append(BookmarkBegin).append(System.lineSeparator());
            bm.append(BookmarkTitle).append(": ")
               .append(current.getTitle()).append(System.lineSeparator());
            bm.append(BookmarkLevel).append(": ")
               .append(level).append(System.lineSeparator());
            
            if (dest instanceof PDPageDestination) {
               PDPageDestination pdest = (PDPageDestination)dest;
               int pageNum = pdest.retrievePageNumber();
               if (pageNum != -1) {
                  bm.append(BookmarkPageNumber).append(": ")
                     .append(pageNum+1).append(System.lineSeparator());
               } else {
                  System.err.println("Error - " + pdest);
               }
            }
            
            // TODO
            if (dest instanceof PDPageXYZDestination) {
               
            }
            else if (dest instanceof PDPageFitDestination) {
               // etc
            }
            
            System.err.println("TODO: " + dest);
         }
         
         exportBookmark(bm, current, level+1);
         current = current.getNextSibling();
      }
   }
   
   @Override
   public void close() throws IOException {
      if (document != null) {
          document.close();
      }
   }
}