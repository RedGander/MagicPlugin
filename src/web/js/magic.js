
var maxXpRegeneration = 100;
var maxSpeedIncrease = 0.7;
var maxRegeneration = 20;
	
function getMaterial(materialKey, iconOnly)
{
	iconOnly = (typeof iconOnly === 'undefined') ? false : iconOnly;
	var materialName = materialKey.replace(/_/g, ' ');
	if (materialKey == 'copy') {
		materialKey = copyMaterial;
		materialName = 'Copy';
		iconOnly = false;
	} else if (materialKey == 'erase') {
		materialKey = eraseMaterial;
		materialName = 'Erase';
		iconOnly = false;
	}
	var imagePath = 'image/material';
	var materialIcon = materialKey.replace(/_/g, '') + '_icon32.png';
	var enclosingSpan = $('<span/>');
	var icon = $('<span title="' + materialName + '" class="materal_icon" style="background-image: url(' + imagePath + '/' + materialIcon + ')">&nbsp;</span>');
	enclosingSpan.append(icon);
	if (!iconOnly) {
		var text = $('<span class="material"/>').text(materialName);
		enclosingSpan.append(text);
	}
	return enclosingSpan;
}

function getSpellDetails(key, showTitle, useMana, costReduction)
{
	showTitle = (typeof showTitle === 'undefined') ? true : showTitle;
	useMana = (typeof useMana === 'undefined') ? false : useMana;
	costReduction = (typeof costReduction === 'undefined') ? 0 : costReduction;
	if (!(key in spells)) {
		return $('<span/>').text("Sorry, something went wrong!");
	}
	var spell = spells[key];
	var detailsDiv = $('<div/>');
	if (showTitle) {
		var title = $('<div class="spellTitleBanner"/>').text(spell.name);
		detailsDiv.append(title);
	}
	var description = $('<div class="spellDescription"/>').text(spell.description);
	var icon = $('<div class="spellIcon"/>');
	icon.append($('<span/>').text('Icon: '));
	icon.append(getMaterial(spell.icon));

	detailsDiv.append(description);
	detailsDiv.append(icon);

	var firstCost = true;
	if ('costs' in spell) {
		detailsDiv.append($('<div class="spellHeading"/>').text('Costs'));
		var costList = $('<ul/>');
		for (var costKey in spell.costs) {
			var amount = spell.costs[costKey];
			if (costReduction > 0) {
				if (costReduction > 1) costReduction = 1;
				amount = amount * (1 - costReduction);
			}
			if (costKey == 'xp') {
				if (useMana) {
					costList.append($('<li/>').text("Mana: " + amount));
				} else {
					costList.append($('<li/>').text("XP: " + amount));
				}
			} else {
				costList.append($('<li/>').append(getMaterial(costKey, true)).append($('<span/>').text(': ' + amount)));
			}
		}
		detailsDiv.append(costList);
	}
	
	if (showTitle) {
		var admin = $('<div class="adminuse"/>').text("Admin use: /wand add " + key);
		detailsDiv.append(admin);
	}
	return detailsDiv;
}

function getLevelString(prefix, amount)
{
	var suffix = "I";

	if (amount >= 1) {
		suffix = "X";
	} else if (amount > 0.8) {
		suffix = "V";
	} else if (amount > 0.6) {
		suffix = "IV";
	} else if (amount > 0.4) {
		suffix = "III";
	} else if (amount > 0.2) {
		suffix = "II";
	}
	return prefix + " " + suffix;
}

function getWandDetails(key)
{
	if (!(key in wands)) {
		return $('<span/>').text("Sorry, something went wrong!");
	}
	var wand = wands[key];
	var detailsDiv = $('<div/>');
	var title = $('<div class="wandTitleBanner"/>').text(wand.name);
	var scrollingContainer = $('<div class="wandContainer"/>');
	var description = $('<div class="wandDescription"/>').text(wand.description);
	var admin = $('<div class="adminuse"/>').text("Admin use: /wand " + key);
	var costReduction = ('cost_reduction' in wand) ? wand['cost_reduction'] : 0;
	var cooldownReduction = ('cooldown_reduction' in wand) ? wand['cooldown_reduction'] : 0;
	var xpRegeneration = ('xp_regeneration' in wand) ? wand['xp_regeneration'] : 0;
	var xpMax = ('xp_max' in wand) ? wand['xp_max'] : 0;
	var hungerRegeneration = ('hunger_regeneration' in wand) ? wand['hunger_regeneration'] : 0;
	var healthRegeneration = ('health_regeneration' in wand) ? wand['health_regeneration'] : 0;
	var uses = ('uses' in wand) ? wand['uses'] : 0;
	var protection = ('protection' in wand) ? wand['protection'] : 0;
	var protectionPhysical = ('protection_physical' in wand) ? wand['protection_physical'] : 0;
	var protectionProjectiles = ('protection_projectiles' in wand) ? wand['protection_projectiles'] : 0;
	var protectionFalling = ('protection_falling' in wand) ? wand['protection_falling'] : 0;
	var protectionFire = ('protection_fire' in wand) ? wand['protection_fire'] : 0;
	var protectionExplosion = ('protection_explosion' in wand) ? wand['protection_explosion'] : 0;
	var power = ('power' in wand) ? wand['power'] : 0;
	var haste = ('haste' in wand) ? wand['haste'] : 0;
	
	detailsDiv.append(title);
	scrollingContainer.append(description);
	
	if (xpRegeneration > 0 && xpMax > 0) {
		scrollingContainer.append($('<div class="mana"/>').text('Mana: ' + xpMax));
		scrollingContainer.append($('<div class="regeneration"/>').text(getLevelString('Mana Regeneration', xpRegeneration / maxXpRegeneration)));
	}
	if (uses > 0) {
		scrollingContainer.append($('<div class="uses"/>').text('Uses: ' + uses));
	}
	if (costReduction > 0) {
		scrollingContainer.append($('<div class="costReduction"/>').text(getLevelString('Cost Reduction', costReduction)));
	}
	if (cooldownReduction > 0) {
		scrollingContainer.append($('<div class="cooldownReduction"/>').text(getLevelString('Cooldown Reduction', cooldownReduction)));
	}
	if (power > 0) {
		scrollingContainer.append($('<div class="power"/>').text(getLevelString('Power', power)));
	}
	if (haste > 0) {
		scrollingContainer.append($('<div class="haste"/>').text(getLevelString('Haste', haste / maxSpeedIncrease)));
	}
	if (protection > 0) {
		scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Protection', protection)));
	}
	if (protection < 1) {
		if (protectionPhysical > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Physical Protection', protectionPhysical)));
		if (protectionProjectiles > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Projectile Protection', protectionProjectiles)));
		if (protectionFalling > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Falling Protection', protectionFalling)));
		if (protectionFire > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Fire Protection', protectionFire)));
		if (protectionExplosion > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Blast Protection', protectionExplosion)));		
	}
	if (healthRegeneration > 0) {
		scrollingContainer.append($('<div class="regeneration"/>').text(getLevelString('Health Regeneration', healthRegeneration / maxRegeneration)));
	}
	if (hungerRegeneration > 0) {
		scrollingContainer.append($('<div class="regeneration"/>').text(getLevelString('Hunger Regeneration', hungerRegeneration / maxRegeneration)));
	}
		
	var spellHeader = $('<div class="wandHeading">Spells</div>');
	var spellListContainer = $('<div id="wandSpellList"/>');
	var spellList = $('<div/>');
	var wandSpells = wand.spells;
	wandSpells.sort();
	for (var spellIndex in wandSpells)
	{
		var key = wand.spells[spellIndex];
		var spell = spells[key];
		spellList.append($('<h3/>').text(spell.name));
		spellList.append($('<div/>').append(getSpellDetails(key, false, xpRegeneration > 0, costReduction)));
	}
	spellList.accordion({ heightStyle: 'content'} );
	spellListContainer.append(spellList);
	scrollingContainer.append(spellHeader);
	scrollingContainer.append(spellListContainer);
	
	if ('materials' in wand && wand.materials.length > 0) {
		var materialHeader = $('<div class="wandHeading">Materials</div>');
		var materialListContainer = $('<ul/>');
		var wandMaterials = wand.materials;
		wandMaterials.sort();
		for (var materialIndex in wandMaterials)
		{
			var key = wandMaterials[materialIndex];
			materialListContainer.append($('<li/>').append(getMaterial(key)));
		}
		scrollingContainer.append(materialHeader);
		scrollingContainer.append(materialListContainer);
	}
	
	detailsDiv.append(scrollingContainer);
	detailsDiv.append(admin);
	return detailsDiv;
}
 
$(document).ready(function() {
	$("#tabs").tabs();
    $("#spellList").selectable({
		selected: function(event, ui) {
			var selected = jQuery(".ui-selected", this);
			var key = selected.prop('id').substr(6);
			$('#spellDetails').empty();
			$('#spellDetails').append(getSpellDetails(key));
		}
    });
    $("#wandList").selectable({
		selected: function(event, ui) {
			var selected = jQuery(".ui-selected", this);
			var key = selected.prop('id').substr(5);
			$('#wandDetails').empty();
			$('#wandDetails').append(getWandDetails(key));
		}
    });
});