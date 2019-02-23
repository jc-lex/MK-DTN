package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AgeBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundleID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PayloadADU;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeDuration;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeInstant;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

final class TestUtilities {
    private TestUtilities() {}
    
    private static final int TEST_FRAGMENT_OFFSET = 0;
    private static final int TEST_EID_LENGTH = 2;
    
    static final String TEST_SENDER = "dtn:b111";
    static final String TEST_RECIPIENT = "dtn:f1b1";
    static final PrimaryBlock.LifeTime TEST_LIFETIME = PrimaryBlock.LifeTime.THREE_DAYS;
    static final PrimaryBlock.PriorityClass TEST_PRIORITY
        = PrimaryBlock.PriorityClass.NORMAL;
    static final Daemon2Router.RoutingProtocol TEST_PROTOCOL
        = Daemon2Router.RoutingProtocol.PER_HOP;
    
    static DTNEndpointID makeDTNEID() {
        DTNEndpointID eid = new DTNEndpointID();
        eid.scheme = DTNEndpointID.DTN_SCHEME;
        eid.ssp = generateRLUUID();
        return eid;
    }
    
    private static String generateRLUUID() {
        int i = 0;
        
        StringBuilder rluuid = new StringBuilder();
        
        while (i < TEST_EID_LENGTH) {
            UUID uuid = UUID.randomUUID();
            rluuid.append(Long.toHexString(uuid.getMostSignificantBits()));
            rluuid.append(Long.toHexString(uuid.getLeastSignificantBits()));
            i++;
        }
        
        return rluuid.toString();
    }
    
    static DTNBundle createTestUserBundle(byte[] message) {
        
        PrimaryBlock primaryBlock = makePrimaryBlockForUserBundle();
    
    
        CanonicalBlock ageCBlock = new CanonicalBlock();
    
        AgeBlock ageBlock = new AgeBlock();
        ageBlock.sourceCPUSpeedInKHz = DTNUtils.getMaxCPUFrequencyInKHz();
        ageBlock.receivingTimestamp = DTNTimeInstant.at(System.currentTimeMillis());
        ageBlock.sendingTimestamp = DTNTimeInstant.at(System.currentTimeMillis());
        ageBlock.agePrime = DTNTimeDuration.ZERO;
        ageBlock.age = DTNTimeDuration.ZERO;
        ageBlock.T = DTNTimeInstant.ZERO;
        
        ageCBlock.blockTypeSpecificDataFields = ageBlock;
        ageCBlock.blockType = CanonicalBlock.BlockType.AGE;
        ageCBlock.mustBeReplicatedInAllFragments = true;
        
        CanonicalBlock payloadCBlock = new CanonicalBlock();
        payloadCBlock.blockTypeSpecificDataFields = makePayloadForUserBundle(message);
        payloadCBlock.blockType = CanonicalBlock.BlockType.PAYLOAD;
        payloadCBlock.mustBeReplicatedInAllFragments = true;
        
        DTNBundle userBundle = new DTNBundle();
        userBundle.primaryBlock = primaryBlock;
        userBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.PAYLOAD, payloadCBlock);
        userBundle.canonicalBlocks.put(DTNBundle.CBlockNumber.AGE, ageCBlock);
        
        return userBundle;
    }
    
    static DTNBundle generateNonFragmentBundle(byte[] message) {
        DTNBundle bundle = createTestUserBundle(message);
        
        bundle.primaryBlock.bundleProcessingControlFlags
            = bundle.primaryBlock.bundleProcessingControlFlags
            .clearBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT);
        
        bundle.primaryBlock.detailsIfFragment.clear();
        
        return bundle;
    }
    
    private static BigInteger generateBundlePCFsForUserBundle() {
        
        return BigInteger.ZERO
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_CUSTODY_TRANSFER_REQUESTED)
            .setBit(PrimaryBlock.BundlePCF.DESTINATION_ENDPOINT_IS_A_SINGLETON)
            .setBit(PrimaryBlock.BundlePCF.BUNDLE_DELIVERY_REPORT_REQUESTED);
    }
    
    private static PrimaryBlock makePrimaryBlockForUserBundle() {
        PrimaryBlock primaryBlock = new PrimaryBlock();
        
        primaryBlock.bundleProcessingControlFlags
            = generateBundlePCFsForUserBundle();
        primaryBlock.priorityClass = TEST_PRIORITY;
        primaryBlock.bundleID = DTNBundleID.from(
            DTNEndpointID.parse(TEST_SENDER),
            DTNTimeInstant.at(System.currentTimeMillis())
        );
        primaryBlock.lifeTime = TEST_LIFETIME.getPeriod();
        primaryBlock.destinationEID = makeDTNEID();
        primaryBlock.custodianEID = makeDTNEID();
        primaryBlock.reportToEID = DTNEndpointID.from(primaryBlock.bundleID.sourceEID);
        
        if (primaryBlock.bundleProcessingControlFlags
            .testBit(PrimaryBlock.BundlePCF.BUNDLE_IS_A_FRAGMENT)) {
            primaryBlock.detailsIfFragment.put(
                PrimaryBlock.FragmentField.FRAGMENT_OFFSET,
                Integer.toString(TEST_FRAGMENT_OFFSET)
            );
            primaryBlock.detailsIfFragment.put(
                PrimaryBlock.FragmentField.TOTAL_ADU_LENGTH,
                Integer.toString(69)
            );
        }
        
        return primaryBlock;
    }
    
    private static PayloadADU makePayloadForUserBundle(byte[] message) {
        
        PayloadADU payload = new PayloadADU();
        payload.ADU = Arrays.copyOf(message, message.length);
        
        return payload;
    }
    
    static final String TEST_SHORT_TEXT_MESSAGE = "William + Phoebe = <3";
    static final String TEST_LONG_TEXT_MESSAGE
        = "  1.  Introduction\n" +
        "\n" +
        "  Welcome, gentle reader.\n" +
        "\n" +
        "  I have written a number of networking HOWTOs in the past, and it\n" +
        "  occurred to me that there's a hell of pile of jargon in each one. I\n" +
        "  had three choices: my other two were ignoring the problem and\n" +
        "  explaining the terms everywhere. Neither was attractive.\n" +
        "\n" +
        "  The point of Free software is that you should have the freedom to\n" +
        "  explore a"
        + "nd play with the software systems you use. I believe that\n" +
        "  enabling people to experience this freedom is a noble goal; not only\n" +
        "  do people feel empowered by the pursuit (such as rebuilding a car\n" +
        "  engine) but the nature of the modern Internet and Free software allows\n" +
        "  you to share the experience with millions.\n" +
        "\n" +
        "  But you have to start somewhere, so here we are.\n" +
        "\n" +
        "  (C) 2000 Paul `Rusty' Russell. Licenced under the GNU GPL.\n" +
        "\n" +
        "  2.  What is a `computer network'?\n" +
        "\n" +
        "\n" +
        "  A computer network is just a set of stuff for nodes to talk to each\n" +
        "  other (by `nodes' I mean computers, printers, Coke machines and\n" +
        "  whatever else you want). It doesn't really matter how they are\n" +
        "  connected: they could use fiber-optic cables or carrier pigeons.\n" +
        "  Obviously, some choices are better than others (especially if you have\n" +
        "  a cat).\n" +
        "\n" +
        "  Usually if you just connect two computers together, it's not called a\n" +
        "  network; you really need three or more to become a network. This is a\n" +
        "  bit like the word `group': two people is just a couple of guys, but\n" +
        "  three can be an `group'. Also, networks are often hooked together, to\n" +
        "  make bigger networks; each little network (usually called a `sub-\n" +
        "  network') can be part of a larger network.\n" +
        "\n" +
        "  The actual connection between two computers is often called a `network\n" +
        "  link'. If there's a bit of cable running out of the back of your\n" +
        "  machine to the other machines, that's your network link.\n" +
        "\n" +
        "  There are four things which we usually care about when we talk about a\n" +
        "  computer network:\n" +
        "\n" +
        "\n" +
        "     Size\n" +
        "\n" +
        "        If you simply connect your four computers at home together, you\n" +
        "        have what is called a LAN (Local Area Network). If everything is\n" +
        "        within walking distance, it's usually called a LAN, however many\n" +
        "        machines are connected to it, and whatever you've built the\n" +
        "        network out of.\n" +
        "\n" +
        "        The other end of the spectrum is a WAN (Wide Area Network).  If\n" +
        "        you have one computer in Lahore, Pakistan, one in Birmingham, UK\n" +
        "        and one in Santiago, Chile, and you manage to connect them, it's\n" +
        "        a WAN.\n" +
        "\n" +
        "     Topology: The Shape\n" +
        "\n" +
        "        Draw a map of the network: lines are the\n" +
        "\n" +
        "        ``network links'', and each node is a dot. Maybe each line leads\n" +
        "        into a central node like a big star, meaning that everyone talks\n" +
        "        through one point (a  `star topology'):\n" +
        "\n" +
        "\n" +
        "              o   o   o\n" +
        "               \\_ | _/\n" +
        "                 \\|/\n" +
        "            o-----o-----o\n" +
        "                _/|\\_\n" +
        "               /  |  \\\n" +
        "              o   o   o\n" +
        "\n" +
        "\n" +
        "\n" +
        "     Maybe everyone talks in a line, like so:\n" +
        "\n" +
        "\n" +
        "              o------o------o-------o--------o\n" +
        "              |                              |\n" +
        "              |                              |\n" +
        "              |                              o\n" +
        "              |                              |\n" +
        "              o                              |\n" +
        "                                             o\n" +
        "\n" +
        "\n" +
        "\n" +
        "     Or maybe you have three subnetworks connected through one node:\n" +
        "\n" +
        "\n" +
        "                          o\n" +
        "              o           |  o--o--o\n" +
        "              |           |  |\n" +
        "              o--o--o--o--o  o\n" +
        "                     \\       |\n" +
        "                      o------o\n" +
        "                     /       |\n" +
        "              o--o--o--o--o  o\n" +
        "              |           |  |\n" +
        "              o           |  o--o\n" +
        "                          o\n" +
        "\n" +
        "\n" +
        "\n" +
        "     You'll see many topologies like these in real life, and many far\n" +
        "     more complicated.\n" +
        "\n" +
        "     Physical: What It's Made Of\n" +
        "        The second thing to care about is what you've built the network\n" +
        "        out of. The cheapest is `sneakernet', where badly-dressed people\n" +
        "        carry floppy disks from one machine to the others. Sneakernet is\n" +
        "        almost always a ``LAN''. Floppies cost less than $1, and a solid\n" +
        "        pair of sneakers can be got for around $20.\n" +
        "\n" +
        "        The most common system used to connect home networks to far\n" +
        "        bigger networks is called a `modem' (for MODulator/DEModulator),\n" +
        "        which turns a normal phone connection into a network link. It\n" +
        "        turns the stuff the computer sends into sounds, and listens to\n" +
        "        sounds coming from the other end to turn them back into stuff\n" +
        "        for the computer.  As you can imagine, this isn't very\n" +
        "        efficient, and phone lines weren't designed for this use, but\n" +
        "        it's popular because phone lines are so common and cheap: modems\n" +
        "        sell for less than $50, and phone lines usually cost a couple of\n" +
        "        hundred dollars a year.\n" +
        "\n" +
        "\n" +
        "\n" +
        "        The most common way to connect machines into a LAN is to use\n" +
        "        Ethernet. Ethernet comes in these main flavors (listed from\n" +
        "        oldest to newest): Thinwire/Coax/10base2, UTP (Unshielded\n" +
        "        Twisted Pair)/10baseT and UTP/100baseT. Gigabit ethernet (the\n" +
        "        name 1000baseT is starting to get silly) is starting to be\n" +
        "        deployed, too. 10base2 wire is usually black coaxial cable, with\n" +
        "        twist-on T-pieces to connect them to things: everyone gets\n" +
        "        connected in a big line, with special `terminator' pieces on the\n" +
        "        two ends. UTP is usually blue wire, with clear `click-in' phone-\n" +
        "        style connectors which plug into sockets to connect: each wire\n" +
        "        connects one node to a central `hub'. The cable is a couple of\n" +
        "        dollars a meter, and the 10baseT/10base2 cards (many cards have\n" +
        "        plugs for both) are hard to get brand new.  100baseT cards,\n" +
        "        which can also speak 10baseT as well, are ten times faster, and\n" +
        "        about $30.\n" +
        "\n" +
        "        On the other end of the spectrum is Fiber; a continuous tiny\n" +
        "        glass filament wrapped in protective coating which can be used\n" +
        "        to run between continents. Generally, fiber costs thousands.\n" +
        "\n" +
        "        We usually call each connection to a node a `network interface',\n" +
        "        or `interface' for short. Linux gives these names like `eth0'\n" +
        "        for the first ethernet interface, and `fddi0' for the first\n" +
        "        fiber interface. The `/sbin/ifconfig' command lists them.\n" +
        "\n" +
        "     Protocol: What It's Speaking\n" +
        "\n" +
        "        The final thing to care about is the language the two are\n" +
        "        speaking. When two ``modems'' are talking to each other down a\n" +
        "        phone line, they need to agree what the different sounds mean,\n" +
        "        otherwise it simply won't work. This convention is called a\n" +
        "        `protocol'. As people discovered new ways of encoding what the\n" +
        "        computer says into smaller sounds, new protocols were invented;\n" +
        "        there are at least a dozen different modem protocols, and most\n" +
        "        modems will try a number of them until they find one the other\n" +
        "        end understands.\n" +
        "\n" +
        "        Another example is the ``100baseT'' network mentioned above: it\n" +
        "        uses the same physical ``network links'' (``UTP'') as\n" +
        "        ``10baseT'' above, but talks ten times as fast.\n" +
        "\n" +
        "\n" +
        "\n" +
        "        These two protocols are what are called `link-level' protocols;\n" +
        "        how stuff is handed over the individual network links, or `one\n" +
        "        hop'. The word `protocol' also refers to other conventions which\n" +
        "        are followed, as we will see next.";
}
