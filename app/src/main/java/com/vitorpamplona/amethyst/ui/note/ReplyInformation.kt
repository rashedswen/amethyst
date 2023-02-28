package com.vitorpamplona.amethyst.ui.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Channel
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.User

@Composable
fun ReplyInformation(replyTo: List<Note>?, mentions: List<User>?, navController: NavController) {
  ReplyInformation(replyTo, mentions) {
    navController.navigate("User/${it.pubkeyHex}")
  }
}

@Composable
fun ReplyInformation(replyTo: List<Note>?, mentions: List<User>?, prefix: String = "", onUserTagClick: (User) -> Unit) {
  FlowRow() {
    if (mentions != null && mentions.isNotEmpty()) {
      if (replyTo != null && replyTo.isNotEmpty()) {
        Text(
          stringResource(R.string.replying_to),
          fontSize = 13.sp,
          color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
        )

        val mentionSet = mentions.toSet()

        mentionSet.toSet().forEachIndexed { idx, user ->
          val innerUserState by user.live().metadata.observeAsState()
          val innerUser = innerUserState?.user

          innerUser?.let { myUser ->
            ClickableText(
              AnnotatedString("${prefix}@${myUser.toBestDisplayName()}"),
              style = LocalTextStyle.current.copy(color = MaterialTheme.colors.primary.copy(alpha = 0.52f), fontSize = 13.sp),
              onClick = { onUserTagClick(myUser) }
            )

            if (idx < mentionSet.size - 2) {
              Text(
                ", ",
                fontSize = 13.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
              )
            } else if (idx < mentionSet.size - 1) {
              Text(
                stringResource(R.string.and),
                fontSize = 13.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
              )
            }
          }
        }
      }
    }
  }
}


@Composable
fun ReplyInformationChannel(replyTo: List<Note>?, mentions: List<User>?, channel: Channel, navController: NavController) {
  ReplyInformationChannel(replyTo, mentions, channel,
    onUserTagClick = {
      navController.navigate("User/${it.pubkeyHex}")
    },
    onChannelTagClick = {
      navController.navigate("Channel/${it.idHex}")
    }
  )
}


@Composable
fun ReplyInformationChannel(replyTo: List<Note>?,
                     mentions: List<User>?,
                     baseChannel: Channel,
                     prefix: String = "",
                     onUserTagClick: (User) -> Unit,
                     onChannelTagClick: (Channel) -> Unit
) {
  val channelState by baseChannel.live.observeAsState()
  val channel = channelState?.channel ?: return

  FlowRow() {
    Text(
      stringResource(R.string.in_channel),
      fontSize = 13.sp,
      color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
    )

    ClickableText(
      AnnotatedString("${channel.info.name} "),
      style = LocalTextStyle.current.copy(color = MaterialTheme.colors.primary.copy(alpha = 0.52f), fontSize = 13.sp),
      onClick = { onChannelTagClick(channel) }
    )

    if (mentions != null && mentions.isNotEmpty()) {
      if (replyTo != null && replyTo.isNotEmpty()) {
        Text(
          stringResource(id = R.string.replying_to),
          fontSize = 13.sp,
          color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
        )

        val mentionSet = mentions.toSet()

        mentionSet.forEachIndexed { idx, user ->
          val innerUserState by user.live().metadata.observeAsState()
          val innerUser = innerUserState?.user

          innerUser?.let { myUser ->
            ClickableText(
              AnnotatedString("${prefix}@${myUser.toBestDisplayName()}"),
              style = LocalTextStyle.current.copy(color = MaterialTheme.colors.primary.copy(alpha = 0.52f), fontSize = 13.sp),
              onClick = { onUserTagClick(myUser) }
            )

            if (idx < mentionSet.size - 2) {
              Text(
                ", ",
                fontSize = 13.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
              )
            } else if (idx < mentionSet.size - 1) {
              Text(
                " ${stringResource(id = R.string.add)} ",
                fontSize = 13.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
              )
            }
          }
        }
      }
    }
  }
}