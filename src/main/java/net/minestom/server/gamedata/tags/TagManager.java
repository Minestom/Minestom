package net.minestom.server.gamedata.tags;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.network.packet.server.play.TagsPacket;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NamespaceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Handles loading and caching of tags
 */
public class TagManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagManager.class);
    private final Gson gson;
    private Map<NamespaceID, Tag> cache = new HashMap<>();
    private List<RequiredTag> requiredTags = new LinkedList<>();

    public TagManager() {
        gson = new GsonBuilder()
                .create();

        addRequiredTag(Tag.BasicTypes.FLUIDS, NamespaceID.from("lava"));
        addRequiredTag(Tag.BasicTypes.FLUIDS, NamespaceID.from("water"));
    }

    /**
     * Loads a tag with the given name. This method attempts to read from "data/&lt;name.domain&gt;/tags/&lt;tagType&gt;/&lt;name.path&gt;.json" if the given name is not already present in cache
     * @param name
     * @param tagType the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @return
     * @throws FileNotFoundException if the file does not exist
     */
    public Tag load(NamespaceID name, String tagType) throws FileNotFoundException {
        return load(name, tagType, () -> new FileReader(new File(ResourceGatherer.DATA_FOLDER, "data/"+name.getDomain()+"/tags/"+tagType+"/"+name.getPath()+".json")));
    }

    /**
     * Loads a tag with the given name. This method attempts to read from 'reader' if the given name is not already present in cache
     * @param name
     * @param tagType the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @param reader
     * @return
     */
    public Tag load(NamespaceID name, String tagType, Reader reader) throws FileNotFoundException {
        return load(name, tagType, () -> reader);
    }

    /**
     * Loads a tag with the given name. This method reads from 'reader'. This will override the previous tag
     * @param name
     * @param tagType the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @param readerSupplier
     * @return
     */
    public Tag forceLoad(NamespaceID name, String tagType, ReaderSupplierWithFileNotFound readerSupplier) throws FileNotFoundException {
        Tag prev = cache.getOrDefault(name, Tag.EMPTY);
        FileNotFoundException[] ex = new FileNotFoundException[1]; // very ugly code but Java does not let its standard interfaces throw exceptions
        Tag result = create(prev, name, tagType, readerSupplier);
        cache.put(name, result);
        return result;
    }

    /**
     * Loads a tag with the given name. This method attempts to read from 'reader' if the given name is not already present in cache
     * @param name
     * @param tagType the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @param readerSupplier
     * @return
     */
    public Tag load(NamespaceID name, String tagType, ReaderSupplierWithFileNotFound readerSupplier) throws FileNotFoundException {
        Tag prev = cache.getOrDefault(name, Tag.EMPTY);
        FileNotFoundException[] ex = new FileNotFoundException[1]; // very ugly code but Java does not let its standard interfaces throw exceptions
        Tag result = cache.computeIfAbsent(name, _name -> {
            try {
                return create(prev, name, tagType, readerSupplier);
            } catch (FileNotFoundException e) {
                ex[0] = e;
                return Tag.EMPTY;
            }
        });
        if(ex[0] != null) {
            throw ex[0];
        }
        return result;
    }

    private Tag create(Tag prev, NamespaceID name, String tagType, ReaderSupplierWithFileNotFound reader) throws FileNotFoundException {
        TagContainer container = gson.fromJson(reader.get(), TagContainer.class);
        try {
            return new Tag(this, name, tagType, prev, container);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to load tag due to error", e);
            return Tag.EMPTY;
        }
    }

    /**
     * Adds the required tags for the game to function correctly
     * @param tags the packet to add the tags to
     */
    public void addRequiredTagsToPacket(TagsPacket tags) {
        for(RequiredTag requiredTag : requiredTags) {
            Tag tag = silentLoad(requiredTag.getName(), requiredTag.getType().name().toLowerCase());
            switch (requiredTag.getType()) {
                case BLOCKS:
                    tags.blockTags.add(tag);
                    break;

                case ITEMS:
                    tags.itemTags.add(tag);
                    break;

                case FLUIDS:
                    tags.fluidTags.add(tag);
                    break;

                case ENTITY_TYPES:
                    tags.entityTags.add(tag);
                    break;
            }
        }
    }

    /**
     * Adds a required tag to send to players when they connect
     * @param type type of tag to send. Required so the client knows its use
     * @param name the name of the tag to load
     */
    public void addRequiredTag(Tag.BasicTypes type, NamespaceID name) {
        requiredTags.add(new RequiredTag(type, name));
    }

    private Tag silentLoad(NamespaceID name, String type) {
        try {
            return load(name, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Tag.EMPTY;
        }
    }

    public interface ReaderSupplierWithFileNotFound {
        Reader get() throws FileNotFoundException;
    }
}
