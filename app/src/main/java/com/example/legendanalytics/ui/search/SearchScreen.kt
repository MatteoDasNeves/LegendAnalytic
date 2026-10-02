package com.example.legendanalytics.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legendanalytics.R
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.SearchHistoryEntry
import com.example.legendanalytics.ui.common.SectionTitle
import com.example.legendanalytics.ui.common.centeredMaxWidth
import com.example.legendanalytics.ui.common.message
import com.example.legendanalytics.ui.navigation.ProfileRoute

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onPlayerFound: (ProfileRoute) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(viewModel) {
        viewModel.navigation.collect { route ->
            keyboard?.hide()
            onPlayerFound(route)
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            item {
                SearchForm(
                    modifier = Modifier.centeredMaxWidth(SearchMaxWidth),
                    state = state,
                    onQueryChange = viewModel::onQueryChange,
                    onRegionChange = viewModel::onRegionChange,
                    onSearch = {
                        keyboard?.hide()
                        viewModel.search()
                    },
                )
                Spacer(Modifier.height(24.dp))
            }
            item {
                Row(Modifier.centeredMaxWidth(SearchMaxWidth), verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle(
                        stringResource(R.string.search_history_title),
                        modifier = Modifier.weight(1f),
                    )
                    if (history.isNotEmpty()) {
                        TextButton(onClick = viewModel::clearHistory) {
                            Text(stringResource(R.string.search_history_clear))
                        }
                    }
                }
                if (history.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.centeredMaxWidth(SearchMaxWidth).padding(vertical = 8.dp),
                    )
                }
            }
            items(history, key = { "${it.region}-${it.riotId}" }) { entry ->
                HistoryItem(
                    entry = entry,
                    enabled = !state.isLoading,
                    onClick = { viewModel.searchFromHistory(entry) },
                    onRemove = { viewModel.removeFromHistory(entry) },
                    modifier = Modifier.centeredMaxWidth(SearchMaxWidth),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/** Un formulaire étiré sur toute une tablette serait illisible : on le borne. */
private val SearchMaxWidth = 560.dp

/** En dessous de cette largeur, la région passe sous le Riot ID pour lui laisser la place. */
private val StackedFormWidth = 340.dp

@Composable
private fun SearchForm(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onRegionChange: (Region) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.app_name).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(stringResource(R.string.search_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.search_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BoxWithConstraints {
            val stacked = maxWidth < StackedFormWidth
            val queryField = @Composable { fieldModifier: Modifier ->
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = fieldModifier,
                    label = { Text(stringResource(R.string.search_riot_id_label)) },
                    placeholder = { Text(stringResource(R.string.search_riot_id_placeholder)) },
                    singleLine = true,
                    isError = state.error != null,
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.search_clear))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                )
            }
            if (stacked) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    queryField(Modifier.fillMaxWidth())
                    RegionSelector(state.region, onRegionChange, Modifier.fillMaxWidth())
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    queryField(Modifier.weight(1f))
                    RegionSelector(state.region, onRegionChange, Modifier.width(112.dp))
                }
            }
        }
        state.error?.let { error ->
            Text(
                text = when (error) {
                    SearchError.InvalidFormat -> stringResource(R.string.search_invalid_format)
                    is SearchError.Api -> error.error.message()
                },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Button(
            onClick = onSearch,
            enabled = !state.isLoading && state.query.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.search_button))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionSelector(
    selected: Region,
    onSelected: (Region) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.search_region_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Region.entries.forEach { region ->
                DropdownMenuItem(
                    text = { Text(region.label) },
                    onClick = {
                        onSelected(region)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(
    entry: SearchHistoryEntry,
    enabled: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(start = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.riotId.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                entry.region.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.search_history_remove))
        }
    }
}
