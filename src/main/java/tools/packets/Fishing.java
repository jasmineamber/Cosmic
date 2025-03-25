/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tools.packets;

import client.Character;
import config.YamlConfig;
import constants.id.ItemId;
import constants.inventory.ItemConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ItemInformationProvider;
import tools.PacketCreator;

import java.util.Calendar;

/**
 * @author FateJiki (RaGeZONE)
 * @author Ronan - timing pattern
 */
public class Fishing {

    private static final Logger log = LoggerFactory.getLogger(Fishing.class);

    private static double getFishingLikelihood(int x) {
        return 50.0 + 7.0 * (7.0 * Math.sin(x)) * (Math.cos(Math.pow(x, 0.777)));
    }

    private static double[] fetchFishingLikelihood() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int hours = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        double yearLikelihood = getFishingLikelihood(dayOfYear);
        double timeLikelihood = getFishingLikelihood(hours + minutes + seconds);
        return new double[]{yearLikelihood, timeLikelihood};
    }

    private static boolean hitFishingTime(Character chr, int baitLevel, double yearLikelihood, double timeLikelihood) {
        double baitLikelihood = 0.0002 * chr.getWorldServer().getFishingRate() * baitLevel;   // can improve 10.0 at "max level 50000" on rate 1x
        if (YamlConfig.config.server.USE_DEBUG) {
            chr.dropMessage(5, "----- FISHING RESULT -----");
            chr.dropMessage(5, "Likelihoods - Year: " + yearLikelihood + " Time: " + timeLikelihood + " Meso: " + baitLikelihood);
            chr.dropMessage(5, "Score rolls - Year: " + (0.23 * yearLikelihood) + " Time: " + (0.77 * timeLikelihood) + " Meso: " + baitLikelihood);
        }
        return (0.23 * yearLikelihood) + (0.77 * timeLikelihood) + (baitLikelihood) > 57.777;
    }

    public static void doFishing(Character chr, int baitLevel) {
        // thanks Fadi, Vcoc for suggesting a custom fishing system

        if (!chr.isLoggedinWorld() || !chr.isAlive()) {
            chr.getWorldServer().unregisterFisherPlayer(chr);
            return;
        }

        if (chr.getLevel() < 30) {
            chr.dropMessage(5, "You must be above level 30 to fish!");
            return;
        }

        String fishingEffect;
        double[] fishingLikelihoods = fetchFishingLikelihood();
        if (!hitFishingTime(chr, baitLevel, fishingLikelihoods[0], fishingLikelihoods[1])) {
            fishingEffect = "Effect/BasicEff.img/Catch/Fail";
        } else {
            fishingEffect = "Effect/BasicEff.img/Catch/Success";
            int rand = (int) (3.0 * Math.random());
            switch (rand) {
                case 0:
                    int mesoAward = (int) (1400.0 * Math.random() + 1201) * chr.getMesoRate() + (15 * chr.getLevel() / 5);
                    chr.gainMeso(mesoAward, true, true, true);
                    break;
                case 1:
                    int expAward = (int) (645.0 * Math.random() + 620.0) * chr.getExpRate() + (15 * chr.getLevel() / 4);
                    chr.gainExp(expAward, true, true);
                    break;
                case 2:
                    int itemId = getRandomItem();
                    if (chr.canHold(itemId)) {
                        chr.getAbstractPlayerInteraction().gainItem(itemId, true);
                    } else {
                        chr.showHint("Couldn't catch a(n) #r" + ItemInformationProvider.getInstance().getName(itemId) + "#k due to #e#b" + ItemConstants.getInventoryType(itemid) + "#k#n inventory limit.");
                    }
                    break;
            }
        }
        chr.sendPacket(PacketCreator.showInfo(fishingEffect));
        chr.getMap().broadcastMessage(chr, PacketCreator.showForeignInfo(chr.getId(), fishingEffect), false);
    }

    public static int getRandomItem() {
        int rand = (int) (100.0 * Math.random());
        int[] commons = {1002851, 2002020, 2002020, ItemId.MANA_ELIXIR, 2000018, 2002018, 2002024, 2002027, 2002027, 2000018, 2000018, 2000018, 2000018, 2002030, 2002018, 2000016}; // filler' up
        int[] uncommons = {1000025, 1002662, 1002812, 1002850, 1002881, 1002880, 1012072, 4020009, 2043220, 2043022, 2040543, 2044420, 2040943, 2043713, 2044220, 2044120, 2040429, 2043220, 2040943}; // filler' uptoo 
        int[] rares = {1002859, 1002553, 1002762, 1002763, 1002764, 1002765, 1002766, 1002663, 1002788, 1002949, 2049100, 2340000, 2040822, 2040822, 2040822, 2040822}; // filler' uplast
        if (rand >= 25) {
            return commons[(int) (commons.length * Math.random())];
        } else if (rand <= 7 && rand >= 4) {
            return uncommons[(int) (uncommons.length * Math.random())];
        } else {
            return rares[(int) (rares.length * Math.random())];
        }
    }
}