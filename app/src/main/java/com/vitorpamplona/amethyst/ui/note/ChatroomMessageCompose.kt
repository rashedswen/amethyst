package com.vitorpamplona.amethyst.ui.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.flowlayout.FlowRow
import com.vitorpamplona.amethyst.NotificationCache
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.RoboHashCache
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.service.model.ChannelCreateEvent
import com.vitorpamplona.amethyst.service.model.ChannelMessageEvent
import com.vitorpamplona.amethyst.service.model.ChannelMetadataEvent
import com.vitorpamplona.amethyst.ui.components.AsyncImageProxy
import com.vitorpamplona.amethyst.ui.components.ResizeImage
import com.vitorpamplona.amethyst.ui.components.TranslateableRichTextViewer
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel

val ChatBubbleShapeMe = RoundedCornerShape(15.dp, 15.dp, 3.dp, 15.dp)
val ChatBubbleShapeThem = RoundedCornerShape(3.dp, 15.dp, 15.dp, 15.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatroomMessageCompose(baseNote: Note, routeForLastRead: String?, innerQuote: Boolean = false, accountViewModel: AccountViewModel, navController: NavController) {
    val noteState by baseNote.live().metadata.observeAsState()
    val note = noteState?.note

    val accountState by accountViewModel.accountLiveData.observeAsState()
    val account = accountState?.account ?: return

    val noteReportsState by baseNote.live().reports.observeAsState()
    val noteForReports = noteReportsState?.note ?: return

    val accountUser = account.userProfile()

    var popupExpanded by remember { mutableStateOf(false) }
    var showHiddenNote by remember { mutableStateOf(false) }

    val context = LocalContext.current.applicationContext

    if (note?.event == null) {
        BlankNote(Modifier)
    } else if (!account.isAcceptable(noteForReports) && !showHiddenNote) {
        HiddenNote(
            account.getRelevantReports(noteForReports),
            account.userProfile(),
            Modifier,
            innerQuote,
            navController,
            onClick = { showHiddenNote = true }
        )
    } else {
        var backgroundBubbleColor: Color
        var alignment: Arrangement.Horizontal
        var shape: Shape

        if (note.author == accountUser) {
            backgroundBubbleColor = MaterialTheme.colors.primary.copy(alpha = 0.32f)
            alignment = Arrangement.End
            shape = ChatBubbleShapeMe
        } else {
            backgroundBubbleColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            alignment = Arrangement.Start
            shape = ChatBubbleShapeThem
        }

        var isNew by remember { mutableStateOf<Boolean>(false) }

        LaunchedEffect(key1 = routeForLastRead) {
            routeForLastRead?.let {
                val lastTime = NotificationCache.load(it, context)

                val createdAt = note.event?.createdAt
                if (createdAt != null) {
                    NotificationCache.markAsRead(it, createdAt, context)
                    isNew = createdAt > lastTime
                }
            }
        }

        Column() {
            Row(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 5.dp,
                        bottom = 5.dp
                    ),
                horizontalArrangement = alignment
            ) {
                Row(
                    horizontalArrangement = alignment,
                    modifier = Modifier.fillMaxWidth(if (innerQuote) 1f else 0.85f)
                ) {

                    Surface(
                        color = backgroundBubbleColor,
                        shape = shape,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { popupExpanded = true }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp),
                        ) {

                            val authorState by note.author!!.live().metadata.observeAsState()
                            val author = authorState?.user!!

                            if (innerQuote || author != accountUser && note.event is ChannelMessageEvent) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = alignment,
                                    modifier = Modifier.padding(top = 5.dp)
                                ) {
                                    AsyncImageProxy(
                                        model = ResizeImage(author.profilePicture(), 25.dp),
                                        placeholder = BitmapPainter(RoboHashCache.get(context, author.pubkeyHex)),
                                        fallback = BitmapPainter(RoboHashCache.get(context, author.pubkeyHex)),
                                        error = BitmapPainter(RoboHashCache.get(context, author.pubkeyHex)),
                                        contentDescription = stringResource(id = R.string.profile_image),
                                        modifier = Modifier
                                            .width(25.dp)
                                            .height(25.dp)
                                            .clip(shape = CircleShape)
                                            .clickable(onClick = {
                                                author?.let {
                                                    navController.navigate("User/${it.pubkeyHex}")
                                                }
                                            })
                                    )

                                    Text(
                                        "  ${author?.toBestDisplayName()}",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable(onClick = {
                                                author?.let {
                                                    navController.navigate("User/${it.pubkeyHex}")
                                                }
                                            })
                                    )
                                }
                            }

                            val replyTo = note.replyTo
                            if (!innerQuote && replyTo != null && replyTo.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    replyTo.toSet().mapIndexed { index, note ->
                                        if (note.event != null)
                                            ChatroomMessageCompose(
                                                note,
                                                null,
                                                innerQuote = true,
                                                accountViewModel = accountViewModel,
                                                navController = navController
                                            )
                                    }
                                }
                            }

                            // TODO: extract String and pass arguments
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val event = note.event
                                if (event is ChannelCreateEvent) {
                                    Text(text = note.author?.toBestDisplayName()
                                        .toString() + " ${stringResource(R.string.created)} " + (event.channelInfo.name
                                        ?: "") +" ${stringResource(R.string.with_description_of)} '" + (event.channelInfo.about
                                        ?: "") + "', ${stringResource(R.string.and_picture)} '" + (event.channelInfo.picture
                                        ?: "") + "'"
                                    )
                                } else if (event is ChannelMetadataEvent) {
                                    Text(text = note.author?.toBestDisplayName()
                                        .toString() + " ${stringResource(R.string.changed_chat_name_to)} '" + (event.channelInfo.name
                                        ?: "") + "$', {stringResource(R.string.description_to)} '" + (event.channelInfo.about
                                        ?: "") + "', ${stringResource(R.string.and_picture_to)} '" + (event.channelInfo.picture
                                        ?: "") + "'"
                                    )
                                } else {
                                    val eventContent = accountViewModel.decrypt(note)

                                    val canPreview = note.author == accountUser
                                          || (note.author?.let { accountUser.isFollowing(it) } ?: true )
                                          || !noteForReports.hasAnyReports()

                                    if (eventContent != null) {
                                        TranslateableRichTextViewer(
                                            eventContent,
                                            canPreview,
                                            Modifier,
                                            note.event?.tags,
                                            accountViewModel,
                                            navController
                                        )
                                    } else {
                                        TranslateableRichTextViewer(
                                            stringResource(R.string.could_not_decrypt_the_message),
                                            true,
                                            Modifier,
                                            note.event?.tags,
                                            accountViewModel,
                                            navController
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    timeAgoLong(note.event?.createdAt, context),
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                                    fontSize = 12.sp
                                )

                                RelayBadges(note)
                            }
                        }
                    }

                }

                NoteDropDownMenu(note, popupExpanded, { popupExpanded = false }, accountViewModel)
            }
        }
    }
}



@Composable
private fun RelayBadges(baseNote: Note) {
    val noteRelaysState by baseNote.live().relays.observeAsState()
    val noteRelays = noteRelaysState?.note?.relays ?: emptySet()

    var expanded by remember { mutableStateOf(false) }

    val relaysToDisplay = if (expanded) noteRelays else noteRelays.take(3)

    val uri = LocalUriHandler.current
    val ctx = LocalContext.current.applicationContext

    FlowRow(Modifier.padding(start = 10.dp)) {
        relaysToDisplay.forEach {
            val url = it.removePrefix("wss://")
            Box(
                Modifier
                    .size(15.dp)
                    .padding(1.dp)) {
                AsyncImage(
                    model = "https://${url}/favicon.ico",
                    placeholder = BitmapPainter(RoboHashCache.get(ctx, url)),
                    fallback = BitmapPainter(RoboHashCache.get(ctx, url)),
                    error = BitmapPainter(RoboHashCache.get(ctx, url)),
                    contentDescription = stringResource(id = R.string.relay_icon),
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                    modifier = Modifier
                        .fillMaxSize(1f)
                        .clip(shape = CircleShape)
                        .background(MaterialTheme.colors.background)
                        .clickable(onClick = { uri.openUri("https://" + url) })
                )
            }
        }

        if (noteRelays.size > 3 && !expanded) {
            IconButton(
                modifier = Modifier.then(Modifier.size(15.dp)),
                onClick = { expanded = true }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                )
            }
        }
    }
}
