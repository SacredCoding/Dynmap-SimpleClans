package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers

import com.google.gson.JsonParser
import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.debug
import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.lang
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper
import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage
import net.sacredlabyrinth.phaed.simpleclans.Clan
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager
import net.sacredlabyrinth.phaed.simpleclans.utils.VanishUtils
import org.dynmap.markers.Marker
import org.dynmap.markers.MarkerAPI

class HomesLayer(
    private val clanManager: ClanManager,
    iconStorage: IconStorage,
    config: LayerConfig,
    markerAPI: MarkerAPI
) : Layer("simpleclans.layers.home", iconStorage, config, markerAPI) {

    init {
        // Delete old markers
        markerSet.markers.forEach(Marker::deleteMarker)

        // Create/update new markers
        getClansWithHome().forEach(this::upsertMarker)
    }

    fun upsertMarker(clan: Clan) {
        val tag = clan.tag
        val loc = clan.homeLocation
        val world = loc?.world ?: return
        val worldName = world.name

        val iconName = JsonParser().parse(clan.flags).asJsonObject.get("defaulticon")?.asString
            ?: iconStorage.defaultIconName
        val icon = iconStorage.getIcon(iconName)
        val label = formatClanLabel(clan)

        if (isHidden(tag, worldName)) {
            debug("Marker can't be updated/created, because it's hidden!")
            return
        }

        val marker = markerSet.findMarker(tag) ?: markerSet.createMarker(
            tag, label, true, worldName, loc.x, loc.y, loc.z, icon, true
        )

        marker.setLocation(worldName, loc.x, loc.y, loc.z)
        marker.setLabel(label, true)
        marker.markerIcon = icon
    }

    private fun isHidden(tag: String, worldName: String): Boolean {
        val hidden = config.section.getStringList("hidden-markers")
        return hidden.contains(tag) || hidden.contains("world:$worldName")
    }

    private fun formatClanLabel(clan: Clan): String? {
        val inactive = "${clan.inactiveDays}/${clan.maxInactiveDays}"

        var onlineMembers =
            clan.onlineMembers.map { cp -> cp.toPlayer() }.count { player -> VanishUtils.isVanished(player) }.toString()

        onlineMembers = "${onlineMembers}/${clan.size}"

        val status = if (clan.isVerified) lang("verified") else lang("unverified")
        val feeEnabled = if (clan.isMemberFeeEnabled) lang("fee-enabled") else lang("fee-disabled")

        val label = config.section.getString("format", "{clan} &8(home)")!!
            .replace("{clan}", clan.name)
            .replace("{tag}", clan.tag)
            .replace("{member_count}", clan.members.size.toString())
            .replace("{inactive}", inactive)
            .replace("{founded}", clan.foundedString)
            .replace("{rival}", clan.totalRival.toString())
            .replace("{neutral}", clan.totalNeutral.toString())
            .replace("{deaths}", clan.totalDeaths.toString())
            .replace("{kdr}", clan.totalKDR.toString())
            .replace("{civilian}", clan.totalCivilian.toString())
            .replace("{members_online}", onlineMembers)
            .replace("{leaders}", clan.getLeadersString("", ", "))
            .replace("{allies}", clan.getAllyString(", ", null))
            .replace("{rivals}", clan.getRivalString(", ", null))
            .replace("{fee_value}", clan.memberFee.toString())
            .replace("{status}", status)
            .replace("{fee_enabled}", feeEnabled)

        return Helper.colorToHTML(label)
    }

    private fun getClansWithHome(): Set<Clan> {
        return clanManager.clans
            .filter { clan -> clan.homeLocation != null }
            .filter { clan -> clan.homeLocation!!.world != null }
            .toSet()
    }
}