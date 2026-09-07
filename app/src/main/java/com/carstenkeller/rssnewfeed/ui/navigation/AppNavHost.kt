package com.carstenkeller.rssnewfeed.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carstenkeller.rssnewfeed.data.repository.FeedRepository
import com.carstenkeller.rssnewfeed.ui.appinfo.InfoScreen
import com.carstenkeller.rssnewfeed.ui.appinfo.InfoViewModel
import com.carstenkeller.rssnewfeed.ui.articledetail.ArticleDetailScreen
import com.carstenkeller.rssnewfeed.ui.articledetail.ArticleDetailViewModel
import com.carstenkeller.rssnewfeed.ui.articlelist.ArticleListScreen
import com.carstenkeller.rssnewfeed.ui.articlelist.ArticleListViewModel
import com.carstenkeller.rssnewfeed.ui.feedmanagement.FeedManagementScreen
import com.carstenkeller.rssnewfeed.ui.feedmanagement.FeedManagementViewModel
import com.carstenkeller.rssnewfeed.ui.searchterms.SearchTermManagementScreen
import com.carstenkeller.rssnewfeed.ui.topics.TopicManagementScreen

private const val ROUTE_LIST = "list"
private const val ROUTE_FEEDS = "feeds"
private const val ROUTE_TOPICS = "topics"
private const val ROUTE_SEARCH_TERMS = "search_terms"
private const val ROUTE_INFO = "info"
private const val ROUTE_ARTICLE = "article/{articleId}"

@Composable
fun AppNavHost(
    repository: FeedRepository,
    deepLinkArticleId: Long? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val appContext = LocalContext.current.applicationContext

    LaunchedEffect(deepLinkArticleId) {
        if (deepLinkArticleId != null) {
            navController.navigate("article/$deepLinkArticleId")
            onDeepLinkConsumed()
        }
    }

    NavHost(navController = navController, startDestination = ROUTE_LIST) {
        composable(ROUTE_LIST) {
            val viewModel: ArticleListViewModel = viewModel(
                factory = viewModelFactory { initializer { ArticleListViewModel(repository, appContext) } },
            )
            ArticleListScreen(
                viewModel = viewModel,
                onOpenArticle = { id -> navController.navigate("article/$id") },
                onOpenFeedManagement = { navController.navigate(ROUTE_FEEDS) },
                onOpenTopicManagement = { navController.navigate(ROUTE_TOPICS) },
                onOpenSearchTermManagement = { navController.navigate(ROUTE_SEARCH_TERMS) },
                onOpenInfo = { navController.navigate(ROUTE_INFO) },
            )
        }
        composable(
            ROUTE_ARTICLE,
            arguments = listOf(navArgument("articleId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getLong("articleId") ?: return@composable
            val viewModel: ArticleDetailViewModel = viewModel(
                factory = viewModelFactory { initializer { ArticleDetailViewModel(repository, articleId) } },
            )
            ArticleDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(ROUTE_FEEDS) {
            val viewModel: FeedManagementViewModel = viewModel(
                factory = viewModelFactory { initializer { FeedManagementViewModel(repository) } },
            )
            FeedManagementScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(ROUTE_TOPICS) {
            TopicManagementScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_SEARCH_TERMS) {
            SearchTermManagementScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_INFO) {
            val viewModel: InfoViewModel = viewModel()
            InfoScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
