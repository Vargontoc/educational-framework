# FEAT-011 - Frontend: World Map Discovery Walk

## Status

state: proposal
user_history: World Map as a discovery walk for the child experience
depends_on: FEAT-007-Game-View-Shell, FEAT-010-Greetings-And-Farwell-Event, docs/contracts/api/openapi.json, docs/contracts/api/websocket.json
owned_by: frontend
scope: frontend implementation of the World Map as a horizontal discovery walk with living world layers, discovery objects, NPC/avatar placeholder support, and animation state preparation. No backend implementation, LearningPath progression logic, activity selection logic, tracking interpretation, or contract changes are included.
test: GameView renders the World Map without level tiles, locked/completed/available nodes, engine icons, visible progress, or child-facing diagnostics; discovery elements use visual-first organic signaling; avatar/NPC placeholder state can be prepared through frontend animation state without owning narrative or progression logic.

## Description

The World Map is the child's main screen and acts as the connection point between activities, exploration, and family accompaniment.

It must not feel like a level selector or a traditional progress map.

The child should feel that they are taking a walk with the avatar, discovering world elements and visiting friendly characters. Progress exists internally in the system, but it is presented as a narrative journey. The child never perceives it as progress.

## Design Principles

### The Map Is A Walk, Not A Level List

The previous LearningPath tile-based design is discarded for this feature.

The World Map does not render nodes, `locked` / `completed` / `available` states, engine icons, or a visible progress path. Any visual representation that suggests a level selector is out of scope for this feature.

The child never sees:

- Numbered levels.
- Progress percentages.
- Level tiles.
- Unlock stars.
- Visible locked elements.

### Curiosity Before Instruction

The avatar does not give direct orders. Its role is to spark curiosity and support conversation between adult and child.

Avoid:

> Tap the cloud.

Prefer:

> That cloud looks interesting.

> I think that frog wants to show us something.

> What could be hidden there?

### The Adult Is Part Of The Experience

The application assumes family accompaniment as the preferred context. The avatar invites discovery, and the adult can naturally reinforce exploration.

The design does not depend exclusively on the adult. When the adult is not available, the avatar acts as a sufficient guide through combined visual and audio signals. The design encourages family presence but does not require it.

### The Child's Behavior Is Not Automatically Interpreted

Ignoring an element, leaving an activity, or not interacting during part of the walk is not interpreted as lack of ability, lack of interest, or learning difficulty.

These behaviors can come from many factors outside the content, such as distraction, tiredness, or family context at that moment. The system does not draw pedagogical conclusions from isolated signals.

Usage information is presented to the parent as descriptive observation, never as diagnosis. Interpretation belongs to the adult, not to the system.

## General Structure

### Current Destination

The walk always has a visible destination: a host character. The destination gives narrative context to the route and is the child's main emotional reference.

Children remember characters better than places. Hosts are the memory anchor of the world.

In v1, while final assets do not exist, the host or NPC can be represented through a visual placeholder. The feature does not require final animations or Lottie assets to be implementable, but the visual structure must allow the placeholder to be replaced by the final character without redesigning the screen.

### Basic Flow

```text
Start walk
      |
Avatar introduces destination with host name
      |
Horizontal scroll through the scenery
      |
World elements: decorative, simple interactive, discovery
      |
Arrival at host character
      |
Narrative microinteraction with host
      |
Soft celebration
      |
Narrative pause: 2-3 seconds
      |
Avatar proposes new destination and automatic transition starts
```

### Destination Transition

The transition is automatic. After the arrival celebration finishes, the avatar makes a continuity comment and the scroll starts toward the next destination without child intervention.

Before starting, there is a 2-3 second pause. Its purpose is to give the child time to assimilate the close of the previous destination before starting the next one. It is not a loading screen and does not require interaction.

### Walk Pace Control

The World Map uses slow automatic horizontal movement, but it is not rigidly continuous. The world moves by itself to create a feeling of travel and discovery, but it responds to the child's exploration.

#### Interaction Pause

When the child touches a world element:

1. The scroll stops temporarily.
2. The element plays its corresponding visual or audio reaction.
3. A small exploration window opens.
4. If the child interacts again during that window, the timer restarts.
5. If there are no new interactions, the walk continues automatically.

The goal is for the child to feel that the world walks with them, not that it drags them forward.

#### Reappearance Of Appreciated Elements

Simple interactive elements can reappear later during the walk. The reappearance does not have to be identical. The system can use small visual or contextual variations to reinforce discovery while avoiding exact repetition.

Examples:

- A cloud can create rain in one appearance and a rainbow in another.
- A bird can sing different sounds.
- A frog can perform different jump animations.

## Host Characters

Hosts are the emotional reference of the world. The same biome can contain several hosts.

### v1 Asset State

Host characters can start as visual placeholders. The v1 goal is to validate the walk model, narrative destination, and organic signaling, not to finalize the animation system.

The frontend can prepare basic visual states for the avatar/NPC even if final animations do not exist yet:

- Idle.
- Waiting.
- Speaking.
- Curious.
- Celebrating.
- Transitioning.

These states do not imply frontend-owned narrative logic. They are a presentation layer prepared to react to current or future events.

### Meadow: v1

- Dog.
- Rabbit.
- Hedgehog.

### Forest: v1.2+

- Owl.
- Squirrel.
- Fox.
- Bear cub.

### Lake: v1.2+

- Frog.
- Duck.
- Beaver.

### Beach: v1.2+

- Crab.
- Turtle.
- Seagull.

## Biome System

### Goal

Allow future scalability without changing the main World Map logic.

### v1

Single scenery: meadow. The destination and host concepts already exist internally.

### v1.2+

Introduce differentiated biomes. Each biome contributes its own background, music, environmental elements, and host characters.

The background component is designed from v1 as an interchangeable slot. The biome is a layer, not hardcoded logic.

## World Elements

The world is composed of three layers with distinct behavior and signaling.

### Layer 1: Living World

Purely decorative elements. They communicate life and movement in the scenery.

Behavior: they move by themselves in periodic and predictable ways. They do not react to the child's touch. They do not emit any interaction signal.

Examples: clouds, flowers, butterflies, birds, leaves.

Signaling: none. Their movement is environmental, not interactive.

### Layer 2: Simple Interactive Elements

Elements that respond to the child's touch. They do not start activities or minigames.

Behavior: no active prior signal. When the child touches them, they provide an immediate, brief, satisfying reaction. The discovery belongs to the child, not to the system.

Examples:

- The frog jumps.
- The dog barks.
- The tree drops leaves.
- The cloud creates rain.

Signaling: very soft idle movement or none. No glow and no call-to-action animation. The avatar can comment on them occasionally, but not systematically.

Consequence: none on progression. They are immediate sensory rewards.

### Layer 3: Discovery Elements

Elements that can start an activity or minigame. They are the entry point to LearningPath content.

Behavior: they emit an active signal to communicate that something is here without looking like a button.

Primary visual signal: a slow inner-to-outer pulsing glow, as if the element were breathing more intensely than the rest of the world. This is the main signal and works independently. It must be enough for the child to identify the element without any audio signal.

Complementary audio signal: when the avatar is active, it always makes a contextual comment when a discovery element appears. This reinforces the visual signal but does not replace it. If the parent has muted the avatar, the visual signal works by itself without a degraded mode.

Examples: curious cloud, mystery box, shiny puddle, special flower.

Interaction: always optional. If the child does not interact, the scroll continues without penalty.

## Visual Philosophy

### Avoid Traditional Video Game Appearance

Do not use:

- Giant objective arrows.
- Mission markers.
- HUD-like signs.
- Numbered tiles.
- Any element that communicates "you have to do something here".

### Organic Signaling

Interesting elements stand out through natural behavior: soft swaying, breathing glow, particular movement. They must not look like interface buttons.

### Visual Hierarchy Of The Three Layers

The difference between layers is perceptible but not explicit:

| Layer | Visual signal | Avatar audio signal |
|---|---|---|
| Living world | Periodic environmental movement | Never |
| Simple interactive | Soft idle movement or none | Occasional |
| Discovery element | Active breathing glow as primary, autonomous signal | Complementary, only when the avatar is active |

The visual signal is always the main channel. Audio is reinforcement, never a dependency.

## Frontend Animation Preparation

Although v1 can work with static placeholders, the frontend can introduce a `useAnimationStore` to centralize the visual state of the avatar/NPC.

The purpose of the store is to prepare the presentation architecture for future animations without introducing domain logic or a premature Lottie dependency.

### `useAnimationStore` Scope

The store can manage simple visual states:

- `idle`
- `waiting`
- `speaking`
- `curious`
- `celebrating`
- `transitioning`

The store can expose presentation actions such as:

- Change the current visual context.
- Activate the speaking state while an avatar event is playing.
- Return to idle after an interaction ends.
- Interrupt a visual animation on navigation, system event, or new interaction.

### Restrictions

The `useAnimationStore` does not decide:

- Which destination to visit.
- Which host appears.
- Which activity a discovery element contains.
- Which phrase the avatar says.
- Whether the child is interested, tired, or blocked.
- How the LearningPath advances.

The store is a visual rendering tool, not a source of truth for the experience.

### Associated Design Criteria

- Animations must be interruptible.
- The experience must work even if final animations do not exist.
- The discovery visual signal must not depend on audio.
- The current placeholder must be replaceable by final assets without changing the main World Map flow.

## Progression And LearningPath

### The LearningPath Is Invisible To The Child

Progression exists in the backend, but the child never perceives it as such. There are no tiles, no visible unlocks, and no progress indicators.

### Discovery Element Content

The type of minigame contained by a discovery element is not associated with the visual appearance of the element. The child cannot predict which activity is waiting. The element is always a surprise. This avoids teaching the child to skip specific activity types by recognizing the element that contains them.

### Ignored Elements

Ignoring a discovery element is not interpreted as a failure or a definitive rejection of the associated content. The backend can use it as a light engagement signal, but never as an isolated pedagogical data point.

If the system detects a consistent pattern of ignored elements or abandoned activities for a specific engine type during the session, it temporarily reduces that engine's priority and rotates toward another content type. When a new session starts, all engines become available again.

The definition of what constitutes a consistent pattern is backend criteria, not part of this frontend feature.

### Parent Dashboard Data

Usage patterns are visible in the Parent Control dashboard as descriptive information:

- Frequently started activities.
- Frequently ignored activities.
- Activities abandoned before completion.
- Average times, successes, and failures by engine type.

The system does not automatically interpret these behaviors. The information supports family observation, not diagnosis.

## Combinatorial Situation System

Characters can repeat. Situations do not.

Each visit is built dynamically by combining:

- Host character: owl, frog, dog, etc.
- Emotional state: curious, happy, surprised, thoughtful.
- Situation: found something, wants to show something, is looking for something, saw something strange.
- Object: cloud, flower, leaf, star, shiny stone.

This creates variety by reusing the same characters without making the child perceive repetition.

## Host Interaction

Arrival at a host is not only a transition between destinations. Each host has a small narrative microinteraction that reinforces their identity as a world character.

### Goals

- Make hosts memorable characters.
- Reinforce the emotional bond with the world.
- Provide narrative closure to the walk.
- Avoid making hosts feel like checkpoints.

### v1 Implementation

Use a short greeting interaction.

Flow:

```text
Arrival at host
      |
Host characteristic animation
      |
Greeting between avatar and host
      |
Small soft visual celebration
      |
Narrative pause: 2-3 seconds
      |
Avatar proposes new destination and automatic transition starts
```

Example avatar phrase:

> We made it! Hello, Frog!

## Minigame Exit

### Inactivity Timeout: Natural Exit For The Child

The minigame engine implements a two-threshold inactivity system:

- First threshold: if the child stops interacting for X seconds, the avatar makes a comment inviting the child to continue the walk.
- Second threshold: if the child still does not interact after the avatar comment, the system automatically returns to the World Map without child action.

The exit is quiet and without drama. The child does not feel that they failed or exited anything. The walk simply continues.

Concrete threshold values are a backend decision and must be added to minigame engine specifications.

### Parent Gesture: Manual Exit

The parent can leave an activity at any time through a long press on the avatar.

After holding the avatar for a few seconds, it performs a short contextual interaction:

> Shall we keep walking?

The system then returns naturally to the World Map.

Design reasons:

- Easy for the adult to remember.
- Consistent across all minigames.
- Does not require visible buttons that break the aesthetic.
- Fits narratively with the avatar's role as the walk companion.

The long press is clearly differentiated from any normal interaction used by minigames.

Pending: define the exact long-press duration and visual feedback behavior during the press in the GameView specifications.

## Behavior Without Adult Present

The avatar acts as a sufficient guide when the adult is not available. The primary visual signal, the breathing glow, allows the child to identify discovery elements even if the avatar is muted.

If the child does not react to an element, the scroll continues and the content reappears later with a different narrative context. There are no repeated calls for attention and no urgency animations.

## Frontend Responsibilities

The frontend scope of this feature is to present the World Map experience as a narrative walk.

The frontend is responsible for:

- Rendering the World Map as a horizontal walk, not a level selector.
- Removing any visual representation based on tiles, nodes, locked states, or visible progress.
- Showing scenery, visual layers, avatar/NPC placeholder, and world elements.
- Visually differentiating living world, simple interactive elements, and discovery elements.
- Applying organic signaling for discovery elements through breathing glow or equivalent visual behavior.
- Keeping the visual signal as the main channel when the avatar is muted or audio fails.
- Managing local visual pauses when the child interacts with world elements.
- Maintaining appropriate touch areas for children aged 3-4.
- Preparing `useAnimationStore` for avatar/NPC visual states.
- Reacting to contracted events without inferring the child's intention, ability, or interest.

The frontend must not:

- Interpret ignored elements, abandoned activities, or lack of interaction as pedagogical signals.
- Decide LearningPath progression.
- Decide engine or activity priority.
- Persist engagement data on its own initiative.
- Show diagnostics, progress percentages, levels, unlocks, or locked states to the child.

## Backend Responsibilities

The backend remains the source of truth for progression, content selection, and descriptive tracking.

The backend is responsible for:

- Deciding the current destination and next destination.
- Selecting host, narrative situation, object, and associated activity.
- Managing the LearningPath that remains invisible to the child.
- Deciding which engine or minigame each discovery element contains.
- Managing consistent patterns of ignored elements, abandoned activities, or engagement when applicable.
- Defining what constitutes a consistent pattern and how it temporarily affects engine priority.
- Defining inactivity thresholds and automatic minigame exit.
- Recording started, ignored, abandoned, and completed activities.
- Providing descriptive data for the parent dashboard.
- Avoiding diagnoses or automatic pedagogical conclusions from isolated signals.

If the backend must send destinations, hosts, world elements, narrative states, or animation metadata, this must be formalized in the contract before full frontend implementation.

## Emotional Goal

The child must not feel that they are advancing through levels.

They must feel that they are exploring the world with their companion and visiting friends.
