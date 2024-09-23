package com.example.imgc.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController


data class BottomNavigationItem(
    val title: String,
    val selectedIcon: Painter,
    val unselectedIcon: Painter,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigation(
    navController: NavController
){
    val items = listOf(
        BottomNavigationItem(
            title = "Home",
            selectedIcon = painterResource(id = R.drawable.baseline_home_24),
            unselectedIcon = painterResource(id = R.drawable.outline_home_24)
        ),
        BottomNavigationItem(
            title = "Plot",
            selectedIcon = painterResource(id = R.drawable.baseline_auto_graph_24),
            unselectedIcon = painterResource(id = R.drawable.outline_auto_graph_24)
        ),
        BottomNavigationItem(
            title = "LeaderBoard",
            selectedIcon = painterResource(id = R.drawable.baseline_leaderboard_24),
            unselectedIcon = painterResource(id = R.drawable.outline_leaderboard_24)
        )
    )
    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    navController.navigate(item.title)
                },
                label = {
                    Text(text = item.title)
                },
                alwaysShowLabel = false,
                icon = {
                    Icon(
                        painter = if (index == selectedItemIndex) {
                            item.selectedIcon
                        } else item.unselectedIcon,
                        contentDescription = item.title
                    )
                })
        }
    }
}