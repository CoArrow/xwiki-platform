/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.doc;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Base class for standard mandatory document initializers.
 *
 * @version $Id$
 * @since 9.0RC1
 */
public abstract class AbstractMandatoryClassInitializer extends AbstractMandatoryDocumentInitializer
{
    /**
     * @param reference the reference of the document to update. Can be either local or absolute depending if the
     *            document is associated to a specific wiki or not
     */
    public AbstractMandatoryClassInitializer(EntityReference reference)
    {
        super(reference);
    }

    /**
     * @param reference the reference of the document to update. Can be either local or absolute depending if the
     *            document is associated to a specific wiki or not
     * @param title the title of the document
     */
    public AbstractMandatoryClassInitializer(EntityReference reference, String title)
    {
        super(reference, title);
    }

    @Override
    public boolean updateDocument(XWikiDocument document)
    {
        boolean needUpdate = super.updateDocument(document);

        // Class related document fields
        needUpdate |= setClassDocumentFields(document);

        // Get document class
        BaseClass currentClass = document.getXClass();

        // Generate the class from scratch
        BaseClass newClass = new BaseClass();
        newClass.setDocumentReference(currentClass.getDocumentReference());
        createClass(newClass);

        // Make sure the current class contains required properties
        needUpdate |= currentClass.apply(newClass, false);

        return needUpdate;
    }

    /**
     * @param xclass the class to create
     * @since 9.0RC1
     */
    protected void createClass(BaseClass xclass)
    {
        // Override
    }

    /**
     * Update document with standard class related properties.
     *
     * @param document the document
     * @return true if the document has been modified, false otherwise
     */
    protected boolean setClassDocumentFields(XWikiDocument document)
    {
        boolean needsUpdate = false;

        // Set the parent since it is different from the current document's space homepage.
        if (document.getParentReference() == null) {
            needsUpdate = true;
            document.setParentReference(new LocalDocumentReference(XWiki.SYSTEM_SPACE, "XWikiClasses"));
        }

        // Use ClassSheet to display the class document if no other sheet is explicitly specified.
        if (this.documentSheetBinder.getSheets(document).isEmpty()) {
            String wikiName = document.getDocumentReference().getWikiReference().getName();
            DocumentReference sheet = new DocumentReference(wikiName, XWiki.SYSTEM_SPACE, "ClassSheet");
            needsUpdate |= this.documentSheetBinder.bind(document, sheet);
        }

        return needsUpdate;
    }
}
