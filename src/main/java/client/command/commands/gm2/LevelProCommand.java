/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.gm2;

import client.Character;
import client.Client;
import client.command.Command;

public class LevelProCommand extends Command {
    {
        setDescription("Set your level, one by one.");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !levelpro <newlevel>");
            return;
        }
        if (params.length == 1) {
            levelUp(player, params[0]);
            return;
        }
        if (player.gmLevel() == 6 && params.length == 2) {
            Character target = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
            if (target == null) {
                player.message("Player '" + params[0] + "' could not be found.");
            } else {
                levelUp(target, params[1]);
            }
        }
    }

    private void levelUp(Character chr, String level) {
        while (chr.getLevel() < Math.min(chr.getMaxClassLevel(), Integer.parseInt(level))) {
            chr.levelUp(false);
        }
    }
}
