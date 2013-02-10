package org.apache.ddlutils.platform.h2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddForeignKeyChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.alteration.RemoveForeignKeyChange;
import org.apache.ddlutils.alteration.RemoveIndexChange;
import org.apache.ddlutils.alteration.TableDefinitionChangesPredicate;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.util.StringUtilsExt;

/**
 * @author Tom Palmer
 * @version $Revision:$
 */
public class H2ModelComparator extends ModelComparator
{
    /**
     * Creates a new MySql model comparator object.
     *
     * @param platformInfo            The platform info
     * @param tableDefChangePredicate The predicate that defines whether tables changes are supported
     *                                by the platform or not; all changes are supported if this is null
     * @param caseSensitive           Whether comparison is case sensitive
     */
    public H2ModelComparator(PlatformInfo platformInfo,
                                TableDefinitionChangesPredicate tableDefChangePredicate,
                                boolean                         caseSensitive)
    {
        super(platformInfo, tableDefChangePredicate, caseSensitive);
    }

    /**
     * {@inheritDoc}
     */
    protected List checkForRemovedIndexes(Database sourceModel,
                                          Table sourceTable,
                                          Database intermediateModel,
                                          Table    intermediateTable,
                                          Database targetModel,
                                          Table    targetTable)
    {
        // Just like MySQL; we need to drop and then recreate FKs that reference columns included in indexes that will be dropped
        List changes = super.checkForRemovedIndexes(sourceModel, sourceTable, intermediateModel, intermediateTable, targetModel, targetTable);
        Set columnNames = new HashSet();

        for (Iterator it = changes.iterator(); it.hasNext();)
        {
            RemoveIndexChange change = (RemoveIndexChange)it.next();
            Index index  = change.findChangedIndex(sourceModel, isCaseSensitive());

            for (int colIdx = 0; colIdx < index.getColumnCount(); colIdx++)
            {
                columnNames.add(index.getColumn(colIdx).getName());
            }
        }
        if (!columnNames.isEmpty())
        {
            // this is only relevant if the columns are referenced by foreign keys in the same table
            changes.addAll(getForeignKeyRecreationChanges(intermediateTable, targetTable, columnNames));
        }

        return changes;
    }

    /**
     * Returns remove and add changes for the foreign keys that reference the indicated columns as a local column.
     *
     * @param intermediateTable The intermediate table
     * @param targetTable       The target table
     * @param columnNames       The names of the columns to look for
     * @return The additional changes
     */
    private List getForeignKeyRecreationChanges(Table intermediateTable, Table targetTable, Set columnNames)
    {
        List newChanges = new ArrayList();

        for (int fkIdx = 0; fkIdx < targetTable.getForeignKeyCount(); fkIdx++)
        {
            ForeignKey targetFk       = targetTable.getForeignKey(fkIdx);
            ForeignKey intermediateFk = intermediateTable.findForeignKey(targetFk, isCaseSensitive());

            if (intermediateFk != null)
            {
                for (int refIdx = 0; refIdx < intermediateFk.getReferenceCount(); refIdx++)
                {
                    Reference ref = intermediateFk.getReference(refIdx);

                    for (Iterator colNameIt = columnNames.iterator(); colNameIt.hasNext();)
                    {
                        if (StringUtilsExt.equals(ref.getLocalColumnName(), (String)colNameIt.next(), isCaseSensitive()))
                        {
                            newChanges.add(new RemoveForeignKeyChange(intermediateTable.getName(), intermediateFk));
                            newChanges.add(new AddForeignKeyChange(intermediateTable.getName(), intermediateFk));
                        }
                    }
                }
            }
        }
        return newChanges;
    }
}
