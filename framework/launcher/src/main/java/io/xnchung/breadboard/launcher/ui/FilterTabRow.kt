package io.xnchung.breadboard.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun FilterTabRow(
    filters: List<CatalogFilter>,
    selectedFilters: Array<CatalogFilter>,
    onSelectFilter: (CatalogFilter) -> Unit
) {
    if (filters.isEmpty()) return

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(filters) { filter ->
            val selected = selectedFilters.contains(filter)
            FilterChip(
                selected = selected, onClick = { onSelectFilter(filter) }, label = {
                    Text(filter.displayName())
                },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Done,
                            contentDescription = "Filter for $filter",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}
